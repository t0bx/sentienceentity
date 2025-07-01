package de.t0bx.sentienceEntity.packet.wrapper.packets;

import de.t0bx.sentienceEntity.packet.utils.EntityType;
import de.t0bx.sentienceEntity.packet.utils.PacketId;
import de.t0bx.sentienceEntity.packet.utils.PacketUtils;
import de.t0bx.sentienceEntity.packet.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Location;

import java.util.UUID;

public class PacketSpawnEntity implements PacketWrapper {

    private final int entityId;
    private final UUID uuid;
    private final EntityType type;
    private final Location location;
    private final float headYaw;
    private final int data;
    private final short velocityX;
    private final short velocityY;
    private final short velocityZ;
    private final int packetId = PacketId.SPAWN_ENTITY.getId();

    /**
     * Constructs a new {@code PacketSpawnEntity} instance used for initializing the spawn entity packet
     * with various parameters including the entity's unique identifier, type, location, orientation,
     * and velocity. The generated packet is utilized to spawn entities on the client side in a multiplayer
     * network setting.
     *
     * @param entityId an integer that uniquely identifies the entity within the current world.
     * @param uuid a globally unique identifier for the entity.
     * @param type the {@code EntityType} of the entity being spawned.
     * @param location the {@code Location} object representing the entity's initial position in the world.
     * @param headYaw the yaw of the entity's head, defining its orientation.
     * @param data additional data associated with the entity, specific to its type.
     * @param velocityX the initial X-axis velocity of the entity, expressed as a short.
     * @param velocityY the initial Y-axis velocity of the entity, expressed as a short.
     * @param velocityZ the initial Z-axis velocity of the entity, expressed as a short.
     */
    public PacketSpawnEntity(int entityId, UUID uuid, EntityType type, Location location,
                             float headYaw, int data, short velocityX, short velocityY, short velocityZ) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.type = type;
        this.location = location;
        this.headYaw = headYaw;
        this.data = data;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
    }


    /**
     * Constructs and serializes a ByteBuf representing the Spawn Entity packet.
     * This method writes various fields such as packet ID, entity ID, UUID, entity type,
     * location coordinates, rotation angles (pitch, yaw, head yaw), entity data, and velocity components
     * to a buffer in the correct format for network transmission.
     *
     * @return a {@code ByteBuf} containing the serialized packet data.
     */
    @Override
    public ByteBuf build() {
        ByteBuf buf = Unpooled.buffer();

        PacketUtils.writeVarInt(buf, packetId);

        PacketUtils.writeVarInt(buf, entityId);
        PacketUtils.writeUUID(buf, uuid);
        PacketUtils.writeVarInt(buf, type.getId());
        PacketUtils.writeDouble(buf, location.getX());
        PacketUtils.writeDouble(buf, location.getY());
        PacketUtils.writeDouble(buf, location.getZ());
        PacketUtils.writeAngle(buf, location.getPitch());
        PacketUtils.writeAngle(buf, location.getYaw());
        PacketUtils.writeAngle(buf, headYaw);
        PacketUtils.writeVarInt(buf, data);
        PacketUtils.writeShort(buf, velocityX);
        PacketUtils.writeShort(buf, velocityY);
        PacketUtils.writeShort(buf, velocityZ);

        return buf;
    }
}
