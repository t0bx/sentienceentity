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

package de.t0bx.sentienceEntity.network.wrapper.packets;

import de.t0bx.sentienceEntity.network.utils.EntityType;
import de.t0bx.sentienceEntity.network.utils.PacketId;
import de.t0bx.sentienceEntity.network.utils.PacketUtils;
import de.t0bx.sentienceEntity.network.version.registries.EntityTypeRegistry;
import de.t0bx.sentienceEntity.network.version.registries.PacketIdRegistry;
import de.t0bx.sentienceEntity.network.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Location;

import java.util.UUID;

public class PacketSpawnEntity implements PacketWrapper {

    private final int entityId;
    private final UUID uuid;
    private final EntityType type;
    private final Location location;
    private final float headYaw;
    private final int data;
    private final short velocityX;
    private final short velocityY;
    private final short velocityZ;
    private final int packetId = PacketIdRegistry.getPacketId(PacketId.SPAWN_ENTITY);

    /**
     * Constructs a new {@code PacketSpawnEntity} instance used for initializing the spawn entity packet
     * with various parameters including the entity's unique identifier, type, location, orientation,
     * and velocity. The generated packet is utilized to spawn entities on the client side in a multiplayer
     * network setting.
     *
     * @param entityId an integer that uniquely identifies the entity within the current world.
     * @param uuid a globally unique identifier for the entity.
     * @param type the {@code EntityType} of the entity being spawned.
     * @param location the {@code Location} object representing the entity's initial position in the world.
     * @param headYaw the yaw of the entity's head, defining its orientation.
     * @param data additional data associated with the entity, specific to its type.
     * @param velocityX the initial X-axis velocity of the entity, expressed as a short.
     * @param velocityY the initial Y-axis velocity of the entity, expressed as a short.
     * @param velocityZ the initial Z-axis velocity of the entity, expressed as a short.
     */
    public PacketSpawnEntity(int entityId, UUID uuid, EntityType type, Location location,
                             float headYaw, int data, short velocityX, short velocityY, short velocityZ) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.type = type;
        this.location = location;
        this.headYaw = headYaw;
        this.data = data;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
    }


    /**
     * Constructs and serializes a ByteBuf representing the Spawn Entity packet.
     * This method writes various fields such as packet ID, entity ID, UUID, entity type,
     * location coordinates, rotation angles (pitch, yaw, head yaw), entity data, and velocity components
     * to a buffer in the correct format for network transmission.
     *
     * @return a {@code ByteBuf} containing the serialized packet data.
     */
    @Override
    public ByteBuf build() {
        ByteBuf buf = Unpooled.buffer();

        PacketUtils.writeVarInt(buf, packetId);

        PacketUtils.writeVarInt(buf, entityId);
        PacketUtils.writeUUID(buf, uuid);
        PacketUtils.writeVarInt(buf, EntityTypeRegistry.getEntityType(type));
        PacketUtils.writeDouble(buf, location.getX());
        PacketUtils.writeDouble(buf, location.getY());
        PacketUtils.writeDouble(buf, location.getZ());
        PacketUtils.writeAngle(buf, location.getPitch());
        PacketUtils.writeAngle(buf, location.getYaw());
        PacketUtils.writeAngle(buf, headYaw);
        PacketUtils.writeVarInt(buf, data);
        PacketUtils.writeShort(buf, velocityX);
        PacketUtils.writeShort(buf, velocityY);
        PacketUtils.writeShort(buf, velocityZ);

        return buf;
    }
}
