package de.t0bx.sentienceEntity.packet.utils;

import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public class PacketUtils {
    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    public static void writeVarInt(ByteBuf buf, int value) {
        while ((value & -128) != 0) {
            buf.writeByte((value & 127) | 128);
            value >>>= 7;
        }
        buf.writeByte(value);
    }

    public static void writeString(ByteBuf buf, String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    public static void writeUUID(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public static void writeBoolean(ByteBuf buf, boolean bool) {
        buf.writeByte(bool ? 1 : 0);
    }

    public static void writeDouble(ByteBuf buf, double value) {
        buf.writeDouble(value);
    }

    public static void writeAngle(ByteBuf buf, float degress) {
        byte angleByte = (byte) (degress * 256.0f / 360.0f);
        buf.writeByte(angleByte);
    }

    public static void writeShort(ByteBuf buf, short value) {
        buf.writeShort(value);
    }

    public static void writeShort(ByteBuf buf, int value) {
        buf.writeShort((short) value);
    }

    public static void writeOptionalComponent(ByteBuf buf, Optional<Component> optional) {
        buf.writeBoolean(true);
        if (optional.isPresent()) {
            buf.writeBoolean(true);

            buf.writeByte(0x0A);
            writeString(buf, "");

            buf.writeByte(0x08);
            writeString(buf, "text");
            writeString(buf, "test");

            buf.writeByte(0x08);
            writeString(buf, "color");
            writeString(buf, "green");

            buf.writeByte(0x00);
        } else {
            buf.writeBoolean(false);
        }
    }
}
