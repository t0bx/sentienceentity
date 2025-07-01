package de.t0bx.sentienceEntity.packet.wrapper.packets;

import de.t0bx.sentienceEntity.packet.utils.PacketId;
import de.t0bx.sentienceEntity.packet.utils.PacketUtils;
import de.t0bx.sentienceEntity.packet.wrapper.PacketWrapper;
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
    private final int packetId = PacketId.TELEPORT_ENTITY.getId();

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
