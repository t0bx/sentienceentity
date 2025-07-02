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

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.packet.PacketPlayer;
import de.t0bx.sentienceEntity.packet.utils.EntityType;
import de.t0bx.sentienceEntity.packet.utils.MetadataEntry;
import de.t0bx.sentienceEntity.packet.utils.MetadataType;
import de.t0bx.sentienceEntity.packet.wrapper.packets.*;
import de.t0bx.sentienceEntity.utils.ReflectionUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
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

    private final Set<PacketPlayer> channels = new HashSet<>();

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
        var addEntityPacket = new PacketSpawnEntity(
                line.getEntityId(),
                UUID.randomUUID(),
                EntityType.ARMOR_STAND,
                location,
                location.getYaw(),
                0,
                (short) 0,
                (short) 0,
                (short) 0
        );

        for (PacketPlayer player : channels) {
            player.sendPacket(addEntityPacket);
        }

        Component component = MiniMessage.miniMessage().deserialize(line.getText());

        var metadata = new PacketSetEntityMetadata(line.getEntityId(), List.of(
                new MetadataEntry(0, MetadataType.BYTE, (byte) 32),
                new MetadataEntry(2, MetadataType.OPTIONAL_TEXT_COMPONENT, Optional.of(component)),
                new MetadataEntry(3, MetadataType.BOOLEAN, true),
                new MetadataEntry(5, MetadataType.BOOLEAN, true),
                new MetadataEntry(15, MetadataType.BYTE, (byte) 25)
        ));

        for (PacketPlayer player : channels) {
            player.sendPacket(metadata);
        }
    }

    public void removeLine(int index) {
        if (!hologramLines.containsKey(index)) return;

        HologramLine line = hologramLines.remove(index);
        if (line == null) return;

        var removeEntityPacket = new PacketRemoveEntities(List.of(line.getEntityId()));
        for (PacketPlayer player : channels) {
            player.sendPacket(removeEntityPacket);
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

            var teleportPacket = new PacketTeleportEntity(
                    line.getEntityId(),
                    newLocation,
                    0, 0, 0,
                    true
            );

            for (PacketPlayer player : channels) {
                player.sendPacket(teleportPacket);
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

            var teleportPacket = new PacketTeleportEntity(
                    line.getEntityId(),
                    newLocation,
                    0, 0, 0,
                    true
            );

            for (PacketPlayer player : channels) {
                player.sendPacket(teleportPacket);
            }
        }
    }

    public void updateLine(int index, String newText) {
        if (!hologramLines.containsKey(index)) return;

        HologramLine line = hologramLines.get(index);
        if (line == null) return;

        line.setText(newText);

        Component component = miniMessage.deserialize(line.getText());

        GsonComponentSerializer gson = GsonComponentSerializer.gson();
        String json = gson.serialize(component);

        var metadata = new PacketSetEntityMetadata(line.getEntityId(), List.of(
                new MetadataEntry(2, MetadataType.OPTIONAL_TEXT_COMPONENT, json)
        ));

        for (PacketPlayer player : this.channels) {
            player.sendPacket(metadata);
        }
    }

    public void spawn(Player player) {
        PacketPlayer packetPlayer = SentienceEntity.getInstance().getPacketController().getPlayer(player);

        if (hasSpawned(packetPlayer)) return;
        if (!player.getWorld().getName().equals(this.getLocation().getWorld().getName())) return;

        channels.add(packetPlayer);

        for (HologramLine line : hologramLines.values()) {
            spawnLine(line);
        }
    }

    public void despawn(Player player) {
        PacketPlayer packetPlayer = SentienceEntity.getInstance().getPacketController().getPlayer(player);

        if (!hasSpawned(packetPlayer)) return;

        for (HologramLine line : hologramLines.values()) {
            var destroyEntity = new PacketRemoveEntities(List.of(line.getEntityId()));
            packetPlayer.sendPacket(destroyEntity);
        }

        channels.remove(packetPlayer);
    }

    public void destroy() {
        for (HologramLine line : hologramLines.values()) {
            var destroyEntity = new PacketRemoveEntities(List.of(line.getEntityId()));
            for (PacketPlayer player : channels) {
                player.sendPacket(destroyEntity);
            }
        }
        channels.clear();
        hologramLines.clear();
    }

    public boolean hasSpawned(PacketPlayer player) {
        return this.channels.contains(player);
    }
}