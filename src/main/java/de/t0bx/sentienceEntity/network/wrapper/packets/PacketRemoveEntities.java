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

import java.util.List;

public class PacketRemoveEntities implements PacketWrapper {

    private final List<Integer> entityIds;
    private final int packetId = PacketIdRegistry.getPacketId(PacketId.REMOVE_ENTITY);

    /**
     * Constructs a new {@code PacketRemoveEntities} instance. This packet is used for removing
     * multiple entities from a game world by their unique identifiers (IDs). The specified list
     * of entity IDs is processed and sent to notify the client to remove the referenced entities.
     *
     * @param entityIds a list of integer IDs, each representing an entity to be removed.
     */
    public PacketRemoveEntities(List<Integer> entityIds) {
        this.entityIds = entityIds;
    }

    /**
     * Constructs and serializes a ByteBuf representing the Remove Entities packet.
     * This method writes the packet ID and a collection of entity IDs into the buffer
     * in the correct format for network transmission. The resulting buffer can be sent
     * to clients to notify them of the removal of entities.
     *
     * @return a {@code ByteBuf} containing the serialized packet data with the packet ID and entity IDs.
     */
    @Override
    public ByteBuf build() {
        ByteBuf buf = Unpooled.buffer();

        PacketUtils.writeVarInt(buf, packetId);

        PacketUtils.writeVarInt(buf, entityIds.size());
        for (Integer id : entityIds) {
            PacketUtils.writeVarInt(buf, id);
        }

        return buf;
    }
}
