package de.t0bx.sentienceEntity.packet.wrapper.packets;

import de.t0bx.sentienceEntity.packet.utils.PacketId;
import de.t0bx.sentienceEntity.packet.utils.PacketUtils;
import de.t0bx.sentienceEntity.packet.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PacketUpdateEntityRotation implements PacketWrapper {

    private final int entityId;
    private final float yaw;
    private final float pitch;
    private final boolean onGround;
    private final int packetId = PacketId.UPDATE_ENTITY_ROTATION.getId();

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
