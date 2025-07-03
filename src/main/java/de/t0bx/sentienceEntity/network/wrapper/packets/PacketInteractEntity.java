package de.t0bx.sentienceEntity.network.wrapper.packets;

import de.t0bx.sentienceEntity.network.interact.InteractHand;
import de.t0bx.sentienceEntity.network.interact.InteractType;
import de.t0bx.sentienceEntity.network.utils.PacketId;
import de.t0bx.sentienceEntity.network.utils.PacketUtils;
import de.t0bx.sentienceEntity.network.version.registries.PacketIdRegistry;
import de.t0bx.sentienceEntity.network.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;

import java.util.Optional;

@Getter
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PacketInteractEntity implements PacketWrapper {

    private final int entityId;
    private final InteractType interactType;
    private final Optional<Float> targetX;
    private final Optional<Float> targetY;
    private final Optional<Float> targetZ;
    private final Optional<InteractHand> hand;
    private final boolean sneaking;
    private final int packetId = PacketIdRegistry.getPacketId(PacketId.INTERACT_ENTITY);

    public PacketInteractEntity(int entityId, InteractType interactType, Optional<Float> targetX, Optional<Float> targetY, Optional<Float> targetZ,
                                Optional<InteractHand> hand, boolean sneaking) {
        this.entityId = entityId;
        this.interactType = interactType;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.hand = hand;
        this.sneaking = sneaking;
    }

    public static PacketInteractEntity read(ByteBuf buf) {
        int entityId = PacketUtils.readVarInt(buf);
        InteractType type = InteractType.from(PacketUtils.readVarInt(buf));

        Optional<Float> x = Optional.empty();
        Optional<Float> y = Optional.empty();
        Optional<Float> z = Optional.empty();
        Optional<InteractHand> hand = Optional.empty();

        switch (type) {
            case INTERACT_AT -> {
                x = Optional.of(buf.readFloat());
                y = Optional.of(buf.readFloat());
                z = Optional.of(buf.readFloat());
                hand = Optional.ofNullable(InteractHand.from(PacketUtils.readVarInt(buf)));
            }
            case INTERACT -> hand = Optional.ofNullable(InteractHand.from(PacketUtils.readVarInt(buf)));
            case ATTACK -> hand = Optional.of(InteractHand.NONE);
            case null -> {}
        }

        boolean sneaking = buf.readBoolean();
        return new PacketInteractEntity(entityId, type, x, y, z, hand, sneaking);
    }

    @Override
    public ByteBuf build() {
        ByteBuf buf = Unpooled.buffer();

        PacketUtils.writeVarInt(buf, packetId);

        PacketUtils.writeVarInt(buf, entityId);
        PacketUtils.writeVarInt(buf, interactType.getId());

        targetX.ifPresent(buf::writeFloat);
        targetY.ifPresent(buf::writeFloat);
        targetZ.ifPresent(buf::writeFloat);
        hand.ifPresent(h -> PacketUtils.writeVarInt(buf, h.getId()));

        PacketUtils.writeBoolean(buf, sneaking);

        return buf;
    }
}
