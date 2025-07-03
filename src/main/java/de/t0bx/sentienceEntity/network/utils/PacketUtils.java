package de.t0bx.sentienceEntity.network.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import de.t0bx.sentienceEntity.network.nbt.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.EncoderException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PacketUtils {

    /**
     * Encodes an integer into a variable-length format and writes it to the provided {@link ByteBuf}.
     * The variable-length encoding uses up to 5 bytes, depending on the magnitude of the input value.
     *
     * @param buf the buffer where the encoded integer will be written
     * @param value the integer value to be encoded and written
     */
    public static void writeVarInt(ByteBuf buf, int value) {
        while ((value & -128) != 0) {
            buf.writeByte((value & 127) | 128);
            value >>>= 7;
        }
        buf.writeByte(value);
    }
    public static int readVarInt(ByteBuf buf) {
        int numRead = 0;
        int result = 0;
        byte read;

        do {
            if (numRead >= 5) {
                throw new RuntimeException("VarInt is too big");
            }

            read = buf.readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
        } while ((read & 0b10000000) != 0);

        return result;
    }

    /**
     * Writes a UTF-8 encoded string to the provided {@link ByteBuf}.
     * The method first encodes the length of the string as a variable-length integer,
     * followed by the UTF-8 bytes of the string itself.
     *
     * @param buf the buffer where the string will be written
     * @param str the string to be encoded and written
     */
    public static void writeString(ByteBuf buf, String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    /**
     * Writes a UTF-8 encoded string with a length prefix as a 16-bit short to the provided {@link ByteBuf}.
     * The method first writes the length of the string (in bytes) as a 16-bit short, followed by the
     * UTF-8 encoded bytes of the string itself.
     *
     * @param buf the buffer to write the string to
     * @param str the string to be encoded and written
     */
    public static void writeShortString(ByteBuf buf, String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(bytes.length);
        buf.writeBytes(bytes);
    }

    /**
     * Writes a UUID to the provided {@link ByteBuf} in a binary format.
     * The UUID is written as two long values: the most significant bits
     * followed by the least significant bits.
     *
     * @param buf the buffer where the UUID will be written
     * @param uuid the UUID to be serialized and written to the buffer
     */
    public static void writeUUID(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    /**
     * Writes a boolean value to the provided {@code ByteBuf} as a single byte.
     * The value {@code true} is written as {@code 1}, and the value {@code false} is written as {@code 0}.
     *
     * @param buf the buffer where the boolean value will be written
     * @param bool the boolean value to be written to the buffer
     */
    public static void writeBoolean(ByteBuf buf, boolean bool) {
        buf.writeByte(bool ? 1 : 0);
    }

    /**
     * Writes a double-precision floating-point value to the provided {@link ByteBuf}.
     *
     * @param buf the buffer where the double value will be written
     * @param value the double value to be written to the buffer
     */
    public static void writeDouble(ByteBuf buf, double value) {
        buf.writeDouble(value);
    }

    /**
     * Encodes a floating-point angle (in degrees) as a single byte and writes it to the provided {@link ByteBuf}.
     * The angle is scaled from the range of 0-360 degrees to a range of 0-255 and then cast to a byte.
     *
     * @param buf the buffer where the encoded angle will be written
     * @param degress the angle in degrees to be encoded and written to the buffer
     */
    public static void writeAngle(ByteBuf buf, float degress) {
        byte angleByte = (byte) (degress * 256.0f / 360.0f);
        buf.writeByte(angleByte);
    }

    /**
     * Writes a 16-bit short value to the provided ByteBuf.
     *
     * @param buf the buffer where the short value will be written
     * @param value the short value to be written to the buffer
     */
    public static void writeShort(ByteBuf buf, short value) {
        buf.writeShort(value);
    }

    /**
     * Writes a 16-bit short value to the provided {@link ByteBuf}.
     * The integer value is cast to a short before being written to the buffer.
     *
     * @param buf the buffer where the short value will be written
     * @param value the integer value to be cast to a short and written to the buffer
     */
    public static void writeShort(ByteBuf buf, int value) {
        buf.writeShort((short) value);
    }

    /**
     * Writes an optional {@link Component} to the provided {@link ByteBuf}.
     * The method first writes a boolean flag indicating whether the optional is present.
     * If the optional contains a value, the component is serialized to JSON,
     * converted into an {@link NbtCompoundTag}, and subsequently written to the buffer using {@code writeTag}.
     *
     * @param buf the buffer where the optional component will be written
     * @param optional the optional component to be serialized and written to the buffer
     */
    public static void writeOptionalComponent(ByteBuf buf, Optional<Component> optional) {
        buf.writeBoolean(optional.isPresent());

        if (optional.isPresent()) {
            Component component = optional.get();
            String json = GsonComponentSerializer.gson().serialize(component);

            NbtCompoundTag tag = fromComponentJson(json);

            try {
                writeTag(tag, new ByteBufOutputStream(buf));
            } catch (Exception exception) {
                throw new EncoderException(exception);
            }
        }
    }

    public static void writeComponent(ByteBuf buf, Component component) {
        String json = GsonComponentSerializer.gson().serialize(component);
        NbtCompoundTag tag = fromComponentJson(json);
        try {
            writeTag(tag, new ByteBufOutputStream(buf));
        } catch (Exception exception) {
            throw new EncoderException(exception);
        }
    }

    private static void writeTag(NbtTag tag, DataOutput output) throws IOException {
        output.writeByte(tag.getTagId());
        if (tag.getTagId() != 0) {
            tag.write(output);
        }
    }

    public static NbtCompoundTag fromComponentJson(String json) {
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("Component JSON must start with an object");
        }

        return convertObjectToTag(root.getAsJsonObject());
    }

    private static NbtCompoundTag convertObjectToTag(JsonObject obj) {
        NbtCompoundTag tag = new NbtCompoundTag();

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            tag.addTag(key, convertJsonElement(value));
        }

        return tag;
    }

    private static NbtTag convertJsonElement(JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();

            if (primitive.isString()) {
                return NbtStringTag.from(primitive.getAsString());
            } else if (primitive.isBoolean()) {
                return new NbtByteTag((byte) (primitive.getAsBoolean() ? 1 : 0));
            } else if (primitive.isNumber()) {
                return new NbtDoubleTag(primitive.getAsDouble());
            }
        } else if (element.isJsonObject()) {
            return convertObjectToTag(element.getAsJsonObject());
        } else if (element.isJsonArray()) {
            NbtListTag list = new NbtListTag();
            for (JsonElement sub : element.getAsJsonArray()) {
                list.add(convertJsonElement(sub));
            }
            return list;
        }

        return NbtStringTag.from("");
    }
}