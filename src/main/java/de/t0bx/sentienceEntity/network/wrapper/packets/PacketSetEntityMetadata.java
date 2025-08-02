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

import de.t0bx.sentienceEntity.network.metadata.MetadataEntry;
import de.t0bx.sentienceEntity.network.metadata.MetadataType;
import de.t0bx.sentienceEntity.network.utils.PacketId;
import de.t0bx.sentienceEntity.network.utils.PacketUtils;
import de.t0bx.sentienceEntity.network.version.registries.PacketIdRegistry;
import de.t0bx.sentienceEntity.network.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;

public class PacketSetEntityMetadata implements PacketWrapper {

    private final int entityId;
    private final List<MetadataEntry> metadata;
    private final int packetId = PacketIdRegistry.getPacketId(PacketId.SET_ENTITY_METADATA);

    /**
     * Constructs a new {@code PacketSetEntityMetadata} instance, which is used to represent
     * an update to an entity's metadata in a multiplayer environment. This metadata includes
     * attributes such as entity state, properties, or animations that are sent to the client.
     *
     * @param entityId an integer representing the unique ID of the entity whose metadata is being updated.
     * @param metadata a list of {@code MetadataEntry} objects, each containing information about
     *                 a metadata index, type, and associated value. These entries describe the modifications
     *                 to the entity's current metadata.
     */
    public PacketSetEntityMetadata(int entityId, List<MetadataEntry> metadata) {
        this.entityId = entityId;
        this.metadata = metadata;
    }

    /**
     * Constructs and serializes a ByteBuf representing the Set Entity Metadata packet.
     * This method writes various fields such as packet ID, entity ID, and metadata entries,
     * including their index, type, and value. Each metadata entry is serialized based on its type,
     * with support for byte, variable integer, float, string, and boolean values. The packet is
     * finalized with a specific termination byte.
     *
     * @return a {@code ByteBuf} containing the serialized packet data for setting entity metadata.
     */
    @Override
    public ByteBuf build() {
        ByteBuf buf = Unpooled.buffer();

        PacketUtils.writeVarInt(buf, packetId);

        PacketUtils.writeVarInt(buf, entityId);

        for (MetadataEntry entry : metadata) {
            buf.writeByte(entry.index);
            PacketUtils.writeVarInt(buf, entry.type.id);

            switch (entry.type) {
                case BYTE -> MetadataType.BYTE.write(buf, entry.value);
                case VAR_INT, POSE -> MetadataType.VAR_INT.write(buf, entry.value);
                case FLOAT -> MetadataType.FLOAT.write(buf, entry.value);
                case STRING -> MetadataType.STRING.write(buf, entry.value);
                case SLOT -> MetadataType.SLOT.write(buf, entry.value);
                case BOOLEAN -> MetadataType.BOOLEAN.write(buf, entry.value);
                case OPTIONAL_TEXT_COMPONENT -> MetadataType.OPTIONAL_TEXT_COMPONENT.write(buf, entry.value);
                default -> throw new UnsupportedOperationException("Unsupported metadata type: " + entry.type);
            }
        }

        buf.writeByte(0xff);
        return buf;
    }
}
