package de.t0bx.sentienceEntity.network.wrapper.packets;

import de.t0bx.sentienceEntity.network.utils.PacketId;
import de.t0bx.sentienceEntity.network.utils.PacketUtils;
import de.t0bx.sentienceEntity.network.version.registries.PacketIdRegistry;
import de.t0bx.sentienceEntity.network.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PacketSetHeadRotation implements PacketWrapper {

    private final int entityId;
    private final float headYaw;
    private final int packetId = PacketIdRegistry.getPacketId(PacketId.SET_HEAD_ROTATION);

    /**
     * Constructs a new {@code PacketSetHeadRotation} instance used for setting the head rotation
     * of an entity in the game. This packet typically updates the client's view of an entity's
     * head orientation based on the specified yaw angle.
     *
     * @param entityId the unique identifier of the entity whose head rotation is being updated.
     * @param headYaw the yaw angle of the entity's head, in degrees, defining its orientation.
     */
    public PacketSetHeadRotation(int entityId, float headYaw) {
        this.entityId = entityId;
        this.headYaw = headYaw;
    }

    /**
     * Builds and serializes a ByteBuf representing the Set Head Rotation packet.
     * This method writes the packet ID, entity ID, and head yaw angle into the buffer
     * in the correct format for network transmission.
     *
     * @return a {@code ByteBuf} containing the serialized packet data.
     */
    @Override
    public ByteBuf build() {
        ByteBuf buf = Unpooled.buffer();

        PacketUtils.writeVarInt(buf, packetId);
        PacketUtils.writeVarInt(buf, entityId);
        PacketUtils.writeAngle(buf, headYaw);

        return buf;
    }
}
