package de.t0bx.sentienceEntity.network.wrapper.packets;

import de.t0bx.sentienceEntity.network.utils.PacketId;
import de.t0bx.sentienceEntity.network.utils.PacketUtils;
import de.t0bx.sentienceEntity.network.version.registries.PacketIdRegistry;
import de.t0bx.sentienceEntity.network.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;
import java.util.UUID;

public class PacketPlayerInfoRemove implements PacketWrapper {

    private final List<UUID> uuids;
    private final int packetId = PacketIdRegistry.getPacketId(PacketId.PLAYER_INFO_REMOVE);

    /**
     * Constructs a new {@code PacketPlayerInfoRemove} instance, which is used to create a packet for
     * removing player information based on a list of unique identifiers (UUIDs). This packet is commonly
     * utilized in a multiplayer environment when specific player data needs to be removed from the client side.
     *
     * @param uuids a list of {@code UUID} objects representing the unique identifiers of the players
     *              whose information is to be removed.
     */
    public PacketPlayerInfoRemove(List<UUID> uuids) {
        this.uuids = uuids;
    }

    /**
     * Constructs and serializes a ByteBuf representing the Player Info Remove packet.
     * This method writes the packet ID and a list of UUIDs into the buffer in the correct
     * format for network transmission. The resulting buffer can be utilized to notify clients
     * to remove the referenced player information from their state.
     *
     * @return a {@code ByteBuf} containing the serialized packet data with the packet ID and UUIDs.
     */
    @Override
    public ByteBuf build() {
        ByteBuf buf = Unpooled.buffer();

        PacketUtils.writeVarInt(buf, packetId);

        PacketUtils.writeVarInt(buf, uuids.size());
        for (UUID uuid : uuids) {
            PacketUtils.writeUUID(buf, uuid);
        }

        return buf;
    }
}
