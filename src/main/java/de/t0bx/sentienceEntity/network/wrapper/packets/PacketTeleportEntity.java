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

import de.t0bx.sentienceEntity.network.utils.PacketId;
import de.t0bx.sentienceEntity.network.utils.PacketUtils;
import de.t0bx.sentienceEntity.network.version.registries.PacketIdRegistry;
import de.t0bx.sentienceEntity.network.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Location;

public class PacketTeleportEntity implements PacketWrapper {

    private final int entityId;
    private final Location location;
    private final double velocityX;
    private final double velocityY;
    private final double velocityZ;
    private final boolean onGround;
    private final int packetId = PacketIdRegistry.getPacketId(PacketId.TELEPORT_ENTITY);

    /**
     * Constructs a new {@code PacketTeleportEntity} instance used for teleporting an entity
     * to a specific location. This packet also includes velocity and on-ground status information
     * for the entity being teleported. It is sent to clients to update the entity's position
     * and movement state in the game world.
     *
     * @param entityId the unique identifier of the entity being teleported.
     * @param location the {@code Location} object representing the target position of the entity.
     * @param velocityX the velocity of the entity along the X-axis.
     * @param velocityY the velocity of the entity along the Y-axis.
     * @param velocityZ the velocity of the entity along the Z-axis.
     * @param onGround a boolean indicating whether the entity is on the ground (true) or in the air (false).
     */
    public PacketTeleportEntity(int entityId, Location location, double velocityX, double velocityY, double velocityZ, boolean onGround) {
        this.entityId = entityId;
        this.location = location;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.onGround = onGround;
    }

    /**
     * Constructs and serializes a {@code ByteBuf} representing the Teleport Entity packet.
     * This method encodes various fields including the packet ID, target location, velocity
     * components, and on-ground status of the entity into a buffer in the correct format
     * for network transmission.
     *
     * @return a {@code ByteBuf} containing the serialized packet data for the Teleport Entity packet.
     */
    @Override
    public ByteBuf build() {
        ByteBuf buf = Unpooled.buffer();

        PacketUtils.writeVarInt(buf, packetId);

        PacketUtils.writeVarInt(buf, entityId);
        PacketUtils.writeDouble(buf, location.getX());
        PacketUtils.writeDouble(buf, location.getY());
        PacketUtils.writeDouble(buf, location.getZ());

        PacketUtils.writeDouble(buf, velocityX);
        PacketUtils.writeDouble(buf, velocityY);
        PacketUtils.writeDouble(buf, velocityZ);

        buf.writeFloat(location.getYaw());
        buf.writeFloat(location.getPitch());

        PacketUtils.writeBoolean(buf, onGround);

        return buf;
    }
}
