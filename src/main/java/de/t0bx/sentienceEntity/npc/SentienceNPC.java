/**
 SentienceEntity API License v1.1
 Copyright (c) 2025 (t0bx)

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to use, copy, modify, and integrate the Software into their own projects, including commercial and closed-source projects, subject to the following conditions:

 1. Attribution:
 You must give appropriate credit to the original author ("Tobias Schuster" or "t0bx"), provide a link to the source or official page if available, and indicate if changes were made. You must do so in a reasonable and visible manner, such as in your plugin.yml, README, or about page.

 2. No Redistribution or Resale:
 You may NOT sell, redistribute, or otherwise make the original Software or modified standalone versions of it available as a product (free or paid), plugin, or downloadable file, unless you have received prior written permission from the author. This includes publishing the plugin on any marketplace (e.g., SpigotMC, MC-Market, Polymart) or including it in paid bundles.

 3. Use as Dependency/API:
 You are allowed to use this Software as a dependency or library in your own plugin or project, including in paid products, as long as attribution is given and the Software itself is not being sold or published separately.

 4. No Misrepresentation:
 You may not misrepresent the origin of the Software. You must clearly distinguish your own modifications from the original work. The original author's name may not be removed from the source files or documentation.

 5. License Retention:
 This license notice and all conditions must be preserved in all copies or substantial portions of the Software.

 6. Disclaimer:
 THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY ARISING FROM THE USE OF THIS SOFTWARE.

 ---

 Summary (non-binding):
 You may use this plugin in your projects, even commercially, but you may not resell or republish it. Always give credit to t0bx.
 */

package de.t0bx.sentienceEntity.npc;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.network.PacketPlayer;
import de.t0bx.sentienceEntity.network.inventory.equipment.Equipment;
import de.t0bx.sentienceEntity.network.inventory.equipment.EquipmentSlot;
import de.t0bx.sentienceEntity.network.metadata.MetadataEntry;
import de.t0bx.sentienceEntity.network.metadata.MetadataType;
import de.t0bx.sentienceEntity.network.utils.*;
import de.t0bx.sentienceEntity.network.wrapper.packets.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;

@Getter
public class SentienceNPC {

    private final String name;

    private final int entityId;

    private final EntityType entityType;

    private final NpcProfile profile;

    @Setter
    private Location location;

    @Setter
    private boolean shouldLookAtPlayer;

    @Setter
    private boolean shouldSneakWithPlayer;

    private final Set<PacketPlayer> channels = new HashSet<>();

    private final EquipmentData equipmentData;

    /**
     * Constructs a new instance of SentienceNPC with the specified entity ID and NPC profile.
     *
     * @param entityId the unique identifier for the NPC entity
     * @param profile the profile containing the NPC's properties such as name, UUID, and skin information
     */
    public SentienceNPC(String name, int entityId, EntityType entityType, NpcProfile profile) {
        this.name = name;
        this.entityId = entityId;
        this.entityType = entityType;
        this.profile = profile;
        this.equipmentData = new EquipmentData();
    }

    /**
     * Spawns the NPC for a specified player by sending necessary packets
     * for adding a player entity, setting entity metadata, and managing team visibility.
     *
     * @param player the Player for whom the NPC should be spawned.
     *               This player will receive the packets to display the NPC.
     */
    public void spawn(Player player) {
        PacketPlayer packetPlayer = SentienceEntity.getInstance().getPacketController().getPlayer(player);

        if (this.hasSpawned(packetPlayer)) return;
        if (this.getLocation() == null) return;

        if (!this.getLocation().getWorld().getName().equalsIgnoreCase(player.getWorld().getName())) return;

        if (entityType == EntityType.PLAYER) {
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
        }

        var addEntityPacket = new PacketSpawnEntity(
                entityId,
                profile.getUuid(),
                entityType,
                location,
                location.getYaw(),
                0,
                (short) 0,
                (short) 0,
                (short) 0
        );

        packetPlayer.sendPacket(addEntityPacket);

        List<MetadataEntry> metadataEntries = new ArrayList<>();
        metadataEntries.add(new MetadataEntry(4, MetadataType.BOOLEAN, true));

        if (entityType == EntityType.PLAYER) {
            metadataEntries.add(new MetadataEntry(17, MetadataType.BYTE, (byte) 127));
        }

        var metadataPacket = new PacketSetEntityMetadata(entityId, metadataEntries);

        packetPlayer.sendPacket(metadataPacket);

        if (entityType == EntityType.PLAYER) {
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

            this.showEquipment(player);
        }

        this.channels.add(packetPlayer);
    }

    /**
     * Updates the rotation of the NPC by setting its yaw and pitch, and sends
     * the corresponding rotation packets to all tracked players. This updates
     * both the entity's overall rotation and its head orientation.
     *
     * @param yaw the yaw angle of the NPC, representing its rotation around the vertical axis in degrees
     * @param pitch the pitch angle of the NPC, representing its rotation around the lateral axis in degrees
     */
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

    /**
     * Updates the sneaking state of the NPC for a specific player and sends a metadata packet
     * to reflect the new state. The NPC's pose metadata is set to indicate whether the NPC
     * is sneaking or not based on the player's sneaking state.
     *
     * @param player the {@link Player} for whom the NPC's sneaking state is being updated.
     */
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

    public void addEquipment(EquipmentSlot slot, ItemStack item) {
        this.equipmentData.getEquipment().add(new Equipment(slot, item));

        var equipmentPacket = new PacketSetEquipment(
                this.entityId,
                this.getEquipmentData().getEquipment()
        );

        for (PacketPlayer player : this.channels) {
            player.sendPacket(equipmentPacket);
        }
    }

    public void removeEquipment(EquipmentSlot slot) {
        this.equipmentData.getEquipment().removeIf(e -> e.getSlot().getId() == slot.getId());

        var equipmentPacket = new PacketSetEquipment(
                this.entityId,
                List.of(new Equipment(slot, new ItemStack(Material.AIR)))
        );

        for (PacketPlayer player : this.channels) {
            player.sendPacket(equipmentPacket);
        }
    }

    public void showEquipment(Player player) {
        PacketPlayer packetPlayer = SentienceEntity.getInstance().getPacketController().getPlayer(player);
        if (!this.hasSpawned(packetPlayer)) return;
        if (this.equipmentData.getEquipment().isEmpty()) return;

        var equipmentPacket = new PacketSetEquipment(
                entityId,
                equipmentData.getEquipment()
        );

        packetPlayer.sendPacket(equipmentPacket);
    }

    /**
     * Updates the NPC's rotation to look at the specified player's eyes. The method calculates
     * the yaw and pitch required to align the NPC's head and body towards the player and
     * sends the corresponding rotation packets to update the NPC's orientation.
     *
     * @param player the {@link Player} whose position the NPC will look at. The NPC's
     *               rotation will be adjusted to face this player's eye location.
     */
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

    /**
     * Despawns the NPC for a specified player by sending a packet that instructs
     * the client to remove the entity represented by this NPC. This removes the NPC
     * from the player's view. If the NPC has not been spawned for the player, the
     * method returns without performing any action.
     *
     * @param player the Player for whom the NPC should be despawned. This player
     *               will receive the packet to remove the NPC entity.
     */
    public void despawn(Player player) {
        PacketPlayer packetPlayer = SentienceEntity.getInstance().getPacketController().getPlayer(player);
        if (!this.hasSpawned(packetPlayer)) return;

        var removeEntityPacket = new PacketRemoveEntities(List.of(this.getEntityId()));
        packetPlayer.sendPacket(removeEntityPacket);
        this.channels.remove(packetPlayer);
    }

    /**
     * Despawns the NPC for all players it is currently visible to. This method sends a
     * {@link PacketRemoveEntities} packet to each {@link PacketPlayer} representing the players
     * tracking the NPC, instructing their clients to remove the NPC entity. Once the packets
     * are sent, the list of channels representing the players tracking the NPC is cleared.
     */
    public void despawnAll() {
        var removeEntitiesPacket = new PacketRemoveEntities(List.of(this.getEntityId()));
        for (PacketPlayer player : this.channels) {
            player.sendPacket(removeEntitiesPacket);
        }
        this.channels.clear();
    }

    /**
     * Teleports the NPC to the specified location and notifies all players currently tracking it
     * by sending a teleport packet. Updates the NPC's position both on the server and for
     * the client-side representation.
     *
     * @param location the {@code Location} object representing the new position of the NPC.
     *                 This includes the coordinates, orientation (yaw and pitch), and world information.
     */
    public void teleport(Location location) {
        this.setLocation(location);
        var entityTeleportPacket = new PacketTeleportEntity(
                this.getEntityId(),
                location,
                0, 0, 0,
                true
        );
        var headRotationPacket = new PacketSetHeadRotation(this.getEntityId(), location.getYaw());

        for (PacketPlayer player : this.channels) {
            player.sendMultiplePackets(
                    entityTeleportPacket,
                    headRotationPacket
            );
        }
    }

    /**
     * Updates the skin of the NPC by modifying its profile properties and re-sending
     * the necessary packets to all players tracking this NPC. The method involves
     * removing the current entity, updating its texture properties with the new skin,
     * and re-adding the entity with updated properties to the players' client views.
     *
     * @param skinValue the base64-encoded value of the new skin texture to be applied
     *                  to the NPC. This value typically includes a URL to the texture.
     * @param skinSignature the signature for the texture value, used to verify the
     *                      authenticity of the skin data.
     */
    public void changeSkin(String skinValue, String skinSignature) {
        if (entityType != EntityType.PLAYER)
            throw new IllegalStateException("Cannot change skin of non-player NPC");

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

    /**
     * Checks whether the specified {@code PacketPlayer} has the NPC spawned for them.
     * The method determines if the {@code PacketPlayer} is included in the list of
     * channels currently tracking this NPC.
     *
     * @param packetPlayer the {@code PacketPlayer} instance representing the player
     *                     for whom to check if the NPC has been spawned.
     * @return {@code true} if the specified {@code PacketPlayer} has the NPC spawned,
     *         {@code false} otherwise.
     */
    public boolean hasSpawned(PacketPlayer packetPlayer) {
        return this.channels.contains(packetPlayer);
    }

    private float normalizeYaw(float yaw) {
        yaw %= 360;
        if (yaw < 0) yaw += 360;
        return yaw;
    }

    @Data
    private static class EquipmentData {
        private final List<Equipment> equipment = new ArrayList<>();
    }
}