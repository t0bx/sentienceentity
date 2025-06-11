/**
 *Creative Commons Attribution-NonCommercial 4.0 International Public License
 * By using this code, you agree to the following terms:
 * You are free to:
 * - Share — copy and redistribute the material in any medium or format
 * - Adapt — remix, transform, and build upon the material
 * Under the following terms:
 * 1. Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made.
 * 2. NonCommercial — You may not use the material for commercial purposes.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 * Full License Text: https://creativecommons.org/licenses/by-nc/4.0/legalcode
 * ---
 * Copyright (c) 2025 t0bx
 * This work is licensed under the Creative Commons Attribution-NonCommercial 4.0 International License.
 */

package de.t0bx.sentienceEntity.hologram;

import de.t0bx.sentienceEntity.utils.ReflectionUtils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

@Getter
public class SentienceHologram {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final double LINE_HEIGHT = 0.25;

    private final int entityId;
    private final UUID uuid;

    private final Location baseLocation;
    private final Map<Integer, HologramLine> hologramLines;

    @Setter
    private Location location;

    private final Set<ServerPlayer> channels = new HashSet<>();

    public SentienceHologram(int entityId, UUID uuid, Location baseLocation) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.baseLocation = baseLocation;

        this.hologramLines = new HashMap<>();
        this.location = baseLocation.clone();
    }

    public void addLine(String line) {
        int lineIndex = hologramLines.size();
        int lineEntityId = ReflectionUtils.generateValidMinecraftEntityId();
        Location lineLocation = this.baseLocation.clone();
        lineLocation.add(0, 1.8 - (LINE_HEIGHT * (lineIndex + 1)), 0);

        HologramLine hologramLine = new HologramLine(lineEntityId, this.uuid, line, lineLocation);
        hologramLines.put(lineIndex, hologramLine);

        spawnLine(hologramLine);
        moveExistingLinesUp();
    }

    private void spawnLine(HologramLine line) {
        Location location = line.getLocation();
        var addEntityPacket = new ClientboundAddEntityPacket(
                line.getEntityId(),
                UUID.randomUUID(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getPitch(),
                location.getYaw(),
                EntityType.ARMOR_STAND,
                0,
                new Vec3(0, 0, 0),
                location.getYaw()
        );

        ServerLevel nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        HolderLookup.Provider provider = nmsWorld.registryAccess();

        net.kyori.adventure.text.Component component = this.miniMessage.deserialize(line.getText());
        String json = GsonComponentSerializer.gson().serialize(component);
        Component nmsComponent = Component.Serializer.fromJson(json, provider);

        if (nmsComponent == null) return;

        var metadata = new ClientboundSetEntityDataPacket(line.getEntityId(), List.of(
                new SynchedEntityData.DataValue<>(0, EntityDataSerializers.BYTE, (byte) 0x20),
                new SynchedEntityData.DataValue<>(2, EntityDataSerializers.OPTIONAL_COMPONENT, Optional.of(nmsComponent)),
                new SynchedEntityData.DataValue<>(3, EntityDataSerializers.BOOLEAN, true),
                new SynchedEntityData.DataValue<>(5, EntityDataSerializers.BOOLEAN, true),
                new SynchedEntityData.DataValue<>(15, EntityDataSerializers.BYTE, (byte) 0x19)
        ));

        for (ServerPlayer player : channels) {
            player.connection.send(addEntityPacket);
            player.connection.send(metadata);
        }
    }

    public void removeLine(int index) {
        if (!hologramLines.containsKey(index)) return;

        HologramLine line = hologramLines.remove(index);
        if (line == null) return;

        var removeEntityPacket = new ClientboundRemoveEntitiesPacket(line.getEntityId());
        for (ServerPlayer player : channels) {
            player.connection.send(removeEntityPacket);
        }

        updateLinesAfterRemoval();
    }

    private void updateLinesAfterRemoval() {
        List<Map.Entry<Integer, HologramLine>> linesList = new ArrayList<>(hologramLines.entrySet());

        linesList.sort(Comparator.comparingInt(Map.Entry::getKey));

        hologramLines.clear();

        for (int newIndex = 0; newIndex < linesList.size(); newIndex++) {
            HologramLine line = linesList.get(newIndex).getValue();

            Location newLocation = baseLocation.clone();
            newLocation.add(0, 1.8 + (LINE_HEIGHT * (linesList.size() - newIndex - 1)), 0);

            line.setLocation(newLocation);

            var teleportPacket = new ClientboundTeleportEntityPacket(
                    line.getEntityId(),
                    new PositionMoveRotation(
                            new Vec3(newLocation.getX(), newLocation.getY(), newLocation.getZ()),
                            new Vec3(0, 0, 0),
                            newLocation.getYaw(),
                            newLocation.getPitch()
                    ),
                    Collections.emptySet(),
                    true
            );

            for (ServerPlayer player : channels) {
                player.connection.send(teleportPacket);
            }

            hologramLines.put(newIndex, line);
        }
    }

    private void moveExistingLinesUp() {
        List<HologramLine> linesList = new ArrayList<>(hologramLines.values());
        for (int i = 0; i < linesList.size(); i++) {
            HologramLine line = linesList.get(i);

            Location newLocation = baseLocation.clone();
            newLocation.add(0, 1.8 + (LINE_HEIGHT * (linesList.size() - i - 1)), 0);

            line.setLocation(newLocation);

            var teleportPacket = new ClientboundTeleportEntityPacket(
                    line.getEntityId(),
                    new PositionMoveRotation(
                            new Vec3(newLocation.getX(), newLocation.getY(), newLocation.getZ()),
                            new Vec3(0, 0, 0),
                            newLocation.getYaw(),
                            newLocation.getPitch()
                    ),
                    Collections.emptySet(),
                    true
            );

            for (ServerPlayer player : channels) {
                player.connection.send(teleportPacket);
            }
        }
    }

    public void updateLine(int index, String newText) {
        if (!hologramLines.containsKey(index)) return;

        HologramLine line = hologramLines.get(index);
        if (line == null) return;

        line.setText(newText);

        ServerLevel nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        HolderLookup.Provider provider = nmsWorld.registryAccess();

        net.kyori.adventure.text.Component component = this.miniMessage.deserialize(line.getText());
        String json = GsonComponentSerializer.gson().serialize(component);
        Component nmsComponent = Component.Serializer.fromJson(json, provider);

        if (nmsComponent == null) return;

        var metadata = new ClientboundSetEntityDataPacket(line.getEntityId(), List.of(
                new SynchedEntityData.DataValue<>(2, EntityDataSerializers.OPTIONAL_COMPONENT, Optional.of(nmsComponent))
        ));

        for (ServerPlayer player : this.channels) {
            player.connection.send(metadata);
        }
    }

    public void spawn(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();

        if (hasSpawned(serverPlayer)) return;
        if (!player.getWorld().getName().equals(this.getLocation().getWorld().getName())) return;

        channels.add(serverPlayer);

        for (HologramLine line : hologramLines.values()) {
            spawnLine(line);
        }
    }

    public void destroy() {
        for (HologramLine line : hologramLines.values()) {
            var destroyEntityPacket = new ClientboundRemoveEntitiesPacket(line.getEntityId());
            for (ServerPlayer player : channels) {
                player.connection.send(destroyEntityPacket);
            }
        }
        channels.clear();
        hologramLines.clear();
    }

    public boolean hasSpawned(ServerPlayer player) {
        return this.channels.contains(player);
    }
}