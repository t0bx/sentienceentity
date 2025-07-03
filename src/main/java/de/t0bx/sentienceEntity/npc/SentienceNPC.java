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

package de.t0bx.sentienceEntity.npc;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.network.PacketPlayer;
import de.t0bx.sentienceEntity.network.metadata.MetadataEntry;
import de.t0bx.sentienceEntity.network.metadata.MetadataType;
import de.t0bx.sentienceEntity.network.utils.*;
import de.t0bx.sentienceEntity.network.wrapper.packets.*;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class SentienceNPC {

    private final int entityId;

    private final NpcProfile profile;

    @Setter
    private Location location;

    @Setter
    private boolean shouldLookAtPlayer;

    @Setter
    private boolean shouldSneakWithPlayer;

    private final Set<PacketPlayer> channels = new HashSet<>();

    public SentienceNPC(int entityId, NpcProfile profile) {
        this.entityId = entityId;
        this.profile = profile;
    }

    public void spawn(Player player) {
        PacketPlayer packetPlayer = SentienceEntity.getInstance().getPacketController().getPlayer(player);

        if (this.hasSpawned(packetPlayer)) return;
        if (this.getLocation() == null) return;

        if (!this.getLocation().getWorld().getName().equalsIgnoreCase(player.getWorld().getName())) return;

        List<PacketPlayerInfoUpdate.Action> actions = List.of(
                PacketPlayerInfoUpdate.Action.ADD_PLAYER
        );
        List<PacketPlayerInfoUpdate.PlayerEntry> entries = List.of(
                new PacketPlayerInfoUpdate.PlayerEntry(
                        profile.getUuid(),
                        profile.getName(),
                        profile.getProperties(),
                        0,
                        false
                )
        );

        var infoUpdatePacket = new PacketPlayerInfoUpdate(actions, entries);

        packetPlayer.sendPacket(infoUpdatePacket);

        var addEntityPacket = new PacketSpawnEntity(
                entityId,
                profile.getUuid(),
                EntityType.PLAYER,
                location,
                location.getYaw(),
                0,
                (short) 0,
                (short) 0,
                (short) 0
        );

        packetPlayer.sendPacket(addEntityPacket);

        List<MetadataEntry> metadataEntries = List.of(
                new MetadataEntry(17, MetadataType.BYTE, (byte) 127)
        );

        var metadataPacket = new PacketSetEntityMetadata(entityId, metadataEntries);

        packetPlayer.sendPacket(metadataPacket);

        String name = "hidden_" + entityId;
        var teamPlayerAddPacket = new PacketSetPlayerTeam(
                name,
                TeamMethods.CREATE_TEAM,
                (byte) 0x01,
                "never",
                "never",
                0,
                List.of(profile.getName())
        );

        packetPlayer.sendPacket(teamPlayerAddPacket);

        this.channels.add(packetPlayer);
    }

    public void updateRotation(float yaw, float pitch) {
        this.getLocation().setYaw(yaw);
        this.getLocation().setPitch(pitch);

        var rotationPacket = new PacketUpdateEntityRotation(
                entityId,
                yaw,
                pitch,
                true
        );

        var headRotationPacket = new PacketSetHeadRotation(entityId, yaw);

        for (PacketPlayer player : this.channels) {
            player.sendPacket(rotationPacket);
            player.sendPacket(headRotationPacket);
        }
    }

    public void updateSneaking(Player player) {
        PacketPlayer packetPlayer = SentienceEntity.getInstance().getPacketController().getPlayer(player);
        if (!this.hasSpawned(packetPlayer)) return;

        boolean playerSneaking = player.isSneaking();
        MetadataEntry entry;
        if (!playerSneaking) {
            entry = new MetadataEntry(6, MetadataType.POSE, 5);
        } else {
            entry = new MetadataEntry(6, MetadataType.POSE, 0);
        }

        var metadataPacket = new PacketSetEntityMetadata(this.getEntityId(), Collections.singletonList(entry));
        packetPlayer.sendPacket(metadataPacket);
    }

    public void updateLookingAtPlayer(Player player) {
        PacketPlayer packetPlayer = SentienceEntity.getInstance().getPacketController().getPlayer(player);
        if (!this.hasSpawned(packetPlayer)) return;

        Location npcLocation = this.getLocation().clone().add(0, 1.62, 0);
        Location playerEyeLocation = player.getEyeLocation();

        double dx = playerEyeLocation.getX() - npcLocation.getX();
        double dy = playerEyeLocation.getY() - npcLocation.getY();
        double dz = playerEyeLocation.getZ() - npcLocation.getZ();

        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        if (distanceXZ == 0) distanceXZ = 0.001;

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distanceXZ));

        yaw = this.normalizeYaw(yaw);

        var entityRotation = new PacketUpdateEntityRotation(
                entityId,
                yaw,
                pitch,
                true
        );

        packetPlayer.sendPacket(entityRotation);

        var headRotationPacket = new PacketSetHeadRotation(entityId, yaw);
        packetPlayer.sendPacket(headRotationPacket);
    }

    public void despawn(Player player) {
        PacketPlayer packetPlayer = SentienceEntity.getInstance().getPacketController().getPlayer(player);
        if (!this.hasSpawned(packetPlayer)) return;

        var removeEntityPacket = new PacketRemoveEntities(List.of(this.getEntityId()));
        packetPlayer.sendPacket(removeEntityPacket);
        this.channels.remove(packetPlayer);
    }

    public void despawnAll() {
        var removeEntitiesPacket = new PacketRemoveEntities(List.of(this.getEntityId()));
        for (PacketPlayer player : this.channels) {
            player.sendPacket(removeEntitiesPacket);
        }
        this.channels.clear();
    }

    public void teleport(Location location) {
        this.setLocation(location);
        var entityTeleportPacket = new PacketTeleportEntity(
                this.getEntityId(),
                location,
                0, 0, 0,
                true
        );
        for (PacketPlayer player : this.channels) {
            player.sendPacket(entityTeleportPacket);
        }
    }

    public void changeSkin(String skinValue, String skinSignature) {
        var removePacket = new PacketPlayerInfoRemove(Collections.singletonList(this.getProfile().getUuid()));
        for (PacketPlayer player : this.channels) {
            player.sendPacket(removePacket);
        }

        var removeEntitiesPacket = new PacketRemoveEntities(List.of(this.getEntityId()));
        for (PacketPlayer player : this.channels) {
            player.sendPacket(removeEntitiesPacket);
        }

        this.getProfile().getProperties().clear();
        this.getProfile().getProperties().add(new PacketPlayerInfoUpdate.Property("textures", skinValue, skinSignature));

        List<PacketPlayerInfoUpdate.Action> actions = List.of(
                PacketPlayerInfoUpdate.Action.ADD_PLAYER
        );
        List<PacketPlayerInfoUpdate.PlayerEntry> entries = List.of(
                new PacketPlayerInfoUpdate.PlayerEntry(
                        profile.getUuid(),
                        "",
                        profile.getProperties(),
                        0,
                        false
                )
        );

        var infoUpdatePacket = new PacketPlayerInfoUpdate(actions, entries);

        for (PacketPlayer player : this.channels) {
            player.sendPacket(infoUpdatePacket);
        }

        var addEntityPacket = new PacketSpawnEntity(
                entityId,
                profile.getUuid(),
                EntityType.PLAYER,
                location,
                location.getYaw(),
                0,
                (short) 0,
                (short) 0,
                (short) 0
        );

        for (PacketPlayer player : this.channels) {
            player.sendPacket(addEntityPacket);
        }

        List<MetadataEntry> metadataEntries = List.of(
                new MetadataEntry(17, MetadataType.BYTE, (byte) 127)
        );

        var metadataPacket = new PacketSetEntityMetadata(entityId, metadataEntries);

        for (PacketPlayer player : this.channels) {
            player.sendPacket(metadataPacket);
        }

        String name = "hidden_" + entityId;
        var teamPlayerAddPacket = new PacketSetPlayerTeam(
                name,
                TeamMethods.CREATE_TEAM,
                (byte) 0x01,
                "never",
                "never",
                0,
                List.of(profile.getName())
        );

        for (PacketPlayer player : this.channels) {
            player.sendPacket(teamPlayerAddPacket);
        }
    }

    public boolean hasSpawned(PacketPlayer packetPlayer) {
        return this.channels.contains(packetPlayer);
    }

    private float normalizeYaw(float yaw) {
        yaw %= 360;
        if (yaw < 0) yaw += 360;
        return yaw;
    }
}