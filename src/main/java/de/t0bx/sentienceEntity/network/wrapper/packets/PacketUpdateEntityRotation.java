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

public class PacketUpdateEntityRotation implements PacketWrapper {

    private final int entityId;
    private final float yaw;
    private final float pitch;
    private final boolean onGround;
    private final int packetId = PacketIdRegistry.getPacketId(PacketId.UPDATE_ENTITY_ROTATION);

    /**
     * Constructs a new {@code PacketUpdateEntityRotation} instance used to update the rotation
     * of an entity in the game world. This packet updates the entity's yaw, pitch, and on-ground state
     * for synchronization with clients.
     *
     * @param entityId the unique identifier of the entity whose rotation is being updated.
     * @param yaw the yaw angle of the entity, representing rotation around the vertical axis in degrees.
     * @param pitch the pitch angle of the entity, representing rotation around the lateral axis in degrees.
     * @param onGround a boolean indicating whether the entity is on the ground or airborne.
     */
    public PacketUpdateEntityRotation(int entityId, float yaw, float pitch, boolean onGround) {
        this.entityId = entityId;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    /**
     * Constructs and serializes a ByteBuf representing the Update Entity Rotation packet.
     * This method writes various fields such as packet ID, entity ID, yaw, pitch, and the on-ground status
     * to a buffer in the correct format for network transmission.
     *
     * @return a {@code ByteBuf} containing the serialized packet data for updating an entity's rotation.
     */
    @Override
    public ByteBuf build() {
        ByteBuf buf = Unpooled.buffer();

        PacketUtils.writeVarInt(buf, packetId);
        PacketUtils.writeVarInt(buf, entityId);
        PacketUtils.writeAngle(buf, yaw);
        PacketUtils.writeAngle(buf, pitch);
        PacketUtils.writeBoolean(buf, onGround);

        return buf;
    }
}
