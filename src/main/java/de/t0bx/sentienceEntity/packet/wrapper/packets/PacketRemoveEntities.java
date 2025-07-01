package de.t0bx.sentienceEntity.packet.wrapper.packets;

import de.t0bx.sentienceEntity.packet.utils.PacketId;
import de.t0bx.sentienceEntity.packet.utils.PacketUtils;
import de.t0bx.sentienceEntity.packet.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;

public class PacketRemoveEntities implements PacketWrapper {

    private final List<Integer> entityIds;
    private final int packetId = PacketId.REMOVE_ENTITY.getId();

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
