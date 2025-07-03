package de.t0bx.sentienceEntity.network.metadata;

import de.t0bx.sentienceEntity.network.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.function.BiConsumer;

public enum MetadataType {
    BYTE(0, (buf, val) -> buf.writeByte((Byte) val)),
    VAR_INT(1, (buf, val) -> PacketUtils.writeVarInt(buf, (Integer) val)),
    FLOAT(3, (buf, val) -> buf.writeFloat((Float) val)),
    STRING(4, (buf, val) -> PacketUtils.writeString(buf, (String) val)),
    BOOLEAN(8, (buf, val) -> buf.writeBoolean((Boolean) val)),
    @SuppressWarnings("unchecked")
    OPTIONAL_TEXT_COMPONENT(6, ((buf, val) -> PacketUtils.writeOptionalComponent(buf, (Optional<Component>) val))),
    POSE(21, (buf, val) -> PacketUtils.writeVarInt(buf, (Integer) val));

    public final int id;
    public final BiConsumer<ByteBuf, Object> writer;

    MetadataType(int id, BiConsumer<ByteBuf, Object> writer) {
        this.id = id;
        this.writer = writer;
    }

    public void write(ByteBuf buf, Object val) {
        writer.accept(buf, val);
    }
}
