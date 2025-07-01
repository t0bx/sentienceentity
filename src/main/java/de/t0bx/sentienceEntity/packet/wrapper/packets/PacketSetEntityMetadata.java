package de.t0bx.sentienceEntity.packet.wrapper.packets;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.packet.utils.MetadataEntry;
import de.t0bx.sentienceEntity.packet.utils.MetadataType;
import de.t0bx.sentienceEntity.packet.utils.PacketId;
import de.t0bx.sentienceEntity.packet.utils.PacketUtils;
import de.t0bx.sentienceEntity.packet.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;

public class PacketSetEntityMetadata implements PacketWrapper {

    private final int entityId;
    private final List<MetadataEntry> metadata;
    private final int packetId = PacketId.SET_ENTITY_METADATA.getId();

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
    @SuppressWarnings("unchecked")
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
                case BOOLEAN -> MetadataType.BOOLEAN.write(buf, entry.value);
                case OPTIONAL_TEXT_COMPONENT -> MetadataType.OPTIONAL_TEXT_COMPONENT.write(buf, entry.value);
                default -> throw new UnsupportedOperationException("Unsupported metadata type: " + entry.type);
            }
        }

        buf.writeByte(0xff);
        return buf;
    }
}
