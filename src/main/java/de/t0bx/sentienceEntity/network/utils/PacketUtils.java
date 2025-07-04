/**
 SentienceEntity API License v1.1
 Copyright (c) 2025 (t0bx)

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to use, copy, modify, and integrate the Software into their own projects, including commercial and closed-source projects, subject to the following conditions:

 1. Attribution:
 You must give appropriate credit to the original author ("Tobias Schuster" or "t0bx"), provide a link to the source or official page if available, and indicate if changes were made. You must do so in a reasonable and visible manner, such as in your plugin.yml, README, or about page.

 2. No Redistribution or Resale:
 You may NOT sell, redistribute, or otherwise make the original Software or modified standalone versions of it available as a product (free or paid), plugin, or downloadable file, unless you have received prior written permission from the author. This includes publishing the plugin on any marketplace (e.g., SpigotMC, MC-Market, Polymart) or including it in paid bundles.

 3. Use as Dependency/API:
 You are allowed to use this Software as a dependency or library in your own plugin or project, including in paid products, as long as attribution is given and the Software itself is not being sold or published separately.

 4. No Misrepresentation:
 You may not misrepresent the origin of the Software. You must clearly distinguish your own modifications from the original work. The original author's name may not be removed from the source files or documentation.

 5. License Retention:
 This license notice and all conditions must be preserved in all copies or substantial portions of the Software.

 6. Disclaimer:
 THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY ARISING FROM THE USE OF THIS SOFTWARE.

 ---

 Summary (non-binding):
 You may use this plugin in your projects, even commercially, but you may not resell or republish it. Always give credit to t0bx.
 */

package de.t0bx.sentienceEntity.network.utils;

import com.google.gson.*;
import de.t0bx.sentienceEntity.network.nbt.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.EncoderException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

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

    /**
     * Reads a variable-length integer (VarInt) from the provided {@link ByteBuf}.
     * A VarInt is a compressed encoding of an integer value, using between 1 and 5 bytes
     * depending on the magnitude of the value. This method decodes the VarInt by
     * reading the bytes sequentially, accumulating the result, and applying the appropriate shifts.
     *
     * If the VarInt exceeds a size of 5 bytes, an exception is thrown to ensure
     * proper decoding and prevent buffer underflows caused by malformed data.
     *
     * @param buf the {@link ByteBuf} from which the VarInt will be read
     * @return the decoded integer value from the VarInt
     * @throws RuntimeException if the VarInt exceeds 5 bytes in length
     */
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

            JsonObject obj = AdventureSerializer.serialize(component);
            NbtCompoundTag tag = convertObjectToTag(obj);
            try {
                writeTag(tag, new ByteBufOutputStream(buf));
            } catch (Exception exception) {
                throw new EncoderException(exception);
            }
        }
    }

    /**
     * Serializes a {@link Component} into a JSON representation, converts it into an {@link NbtCompoundTag},
     * and writes it to the specified {@link ByteBuf}.
     * This method ensures that the {@link Component} is properly encoded and stored in a binary format
     * that can later be deserialized and reconstructed.
     *
     * @param buf the buffer where the serialized {@link Component} will be written
     * @param component the {@link Component} to serialize and write to the buffer
     * @throws EncoderException if an error occurs during the encoding process
     */
    public static void writeComponent(ByteBuf buf, Component component) {
        String json = GsonComponentSerializer.gson().serialize(component);
        NbtCompoundTag tag = fromComponentJson(json);
        try {
            writeTag(tag, new ByteBufOutputStream(buf));
        } catch (Exception exception) {
            throw new EncoderException(exception);
        }
    }

    /**
     * Parses a JSON string representing a component and converts it into an {@link NbtCompoundTag}.
     * The input JSON must represent a JSON object; otherwise, an {@link IllegalArgumentException} is thrown.
     *
     * @param json the JSON string representation of the component to be converted
     * @return an {@link NbtCompoundTag} representing the parsed component
     * @throws IllegalArgumentException if the JSON input does not start with a JSON object
     */
    public static NbtCompoundTag fromComponentJson(String json) {
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("Component JSON must start with an object");
        }

        return convertObjectToTag(root.getAsJsonObject());
    }

    private static void writeTag(NbtTag tag, DataOutput output) throws IOException {
        output.writeByte(tag.getTagId());
        if (tag.getTagId() != 0) {
            tag.write(output);
        }
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
        if (element.isJsonNull()) {
            return NbtStringTag.from("");
        }

        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();

            if (primitive.isString()) {
                return NbtStringTag.from(primitive.getAsString());
            } else if (primitive.isBoolean()) {
                return new NbtByteTag((byte) (primitive.getAsBoolean() ? 1 : 0));
            } else if (primitive.isNumber()) {
                String numberStr = primitive.getAsString();

                if (numberStr.contains(".")) {
                    double doubleValue = primitive.getAsDouble();
                    float floatValue = primitive.getAsFloat();

                    if (Math.abs(doubleValue - floatValue) < 1e-7 && Math.abs(doubleValue) <= Float.MAX_VALUE) {
                        return new NbtFloatTag(floatValue);
                    } else {
                        return new NbtDoubleTag(doubleValue);
                    }
                } else {
                    long longValue = primitive.getAsLong();

                    if (longValue >= Byte.MIN_VALUE && longValue <= Byte.MAX_VALUE) {
                        return new NbtByteTag((byte) longValue);
                    } else if (longValue >= Short.MIN_VALUE && longValue <= Short.MAX_VALUE) {
                        return new NbtShortTag((short) longValue);
                    } else if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                        return new NbtIntTag((int) longValue);
                    } else {
                        return new NbtLongTag(longValue);
                    }
                }
            }
        } else if (element.isJsonObject()) {
            return convertObjectToTag(element.getAsJsonObject());
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();

            if (!array.isEmpty() && array.get(0).isJsonPrimitive()) {
                JsonPrimitive first = array.get(0).getAsJsonPrimitive();

                if (first.isNumber()) {
                    boolean allIntegers = true;
                    boolean allBytes = true;
                    boolean allLongs = true;

                    for (JsonElement elem : array) {
                        if (!elem.isJsonPrimitive() || !elem.getAsJsonPrimitive().isNumber()) {
                            allIntegers = false;
                            break;
                        }

                        long value = elem.getAsLong();
                        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                            allBytes = false;
                        }
                        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                            allLongs = false;
                        }
                    }

                    if (allIntegers) {
                        if (allBytes) {
                            byte[] bytes = new byte[array.size()];
                            for (int i = 0; i < array.size(); i++) {
                                bytes[i] = array.get(i).getAsByte();
                            }
                            return new NbtByteArrayTag(bytes);
                        } else if (!allLongs) {
                            int[] ints = new int[array.size()];
                            for (int i = 0; i < array.size(); i++) {
                                ints[i] = array.get(i).getAsInt();
                            }
                            return new NbtIntArrayTag(ints);
                        } else {
                            long[] longs = new long[array.size()];
                            for (int i = 0; i < array.size(); i++) {
                                longs[i] = array.get(i).getAsLong();
                            }
                            return new NbtLongArrayTag(longs);
                        }
                    }
                }
            }

            NbtListTag list = new NbtListTag();
            for (JsonElement sub : array) {
                list.add(convertJsonElement(sub));
            }
            return list;
        }

        return NbtStringTag.from("");
    }

    public static NbtCompoundTag serializeComponent(Component component) {
        NbtCompoundTag tag = new NbtCompoundTag();

        if (component instanceof TextComponent textComponent) {
            tag.addTag("text", NbtStringTag.from(textComponent.content()));
        }

        else if (component instanceof TranslatableComponent translatable) {
            tag.addTag("translate", NbtStringTag.from(translatable.key()));
            if (translatable.fallback() != null) {
                tag.addTag("fallback", NbtStringTag.from(translatable.fallback()));
            }

            List<TranslationArgument> args = translatable.arguments();
            if (!args.isEmpty()) {
                NbtListTag list = new NbtListTag();
                for (TranslationArgument arg : args) {
                    list.add(serializeComponent(arg.asComponent()));
                }
                tag.addTag("with", list);
            }
        }

        serializeStyle(tag, component.style());

        List<Component> children = component.children();
        if (!children.isEmpty()) {
            NbtListTag extra = new NbtListTag();
            for (Component child : children) {
                extra.add(serializeComponent(child));
            }
            tag.addTag("extra", extra);
        }

        return tag;
    }

    private static void serializeStyle(NbtCompoundTag tag, Style style) {
        if (style.color() != null) {
            tag.addTag("color", NbtStringTag.from(style.color().asHexString()));
        }

        if (style.font() != null) {
            tag.addTag("font", NbtStringTag.from(style.font().asString()));
        }

        for (TextDecoration deco : TextDecoration.values()) {
            TextDecoration.State state = style.decoration(deco);
            if (state != TextDecoration.State.NOT_SET) {
                tag.addTag(deco.toString(), NbtStringTag.from(state.name().toLowerCase(Locale.ROOT)));
            }
        }

        if (style.insertion() != null) {
            tag.addTag("insertion", NbtStringTag.from(style.insertion()));
        }

        ClickEvent click = style.clickEvent();
        if (click != null) {
            NbtCompoundTag clickTag = new NbtCompoundTag();
            clickTag.addTag("action", NbtStringTag.from(click.action().toString()));
            clickTag.addTag("value", NbtStringTag.from(click.value()));
            tag.addTag("clickEvent", clickTag);
        }

        HoverEvent<?> hover = style.hoverEvent();
        if (hover != null) {
            NbtCompoundTag hoverTag = new NbtCompoundTag();
            hoverTag.addTag("action", NbtStringTag.from(hover.action().toString()));

            if (hover.value() instanceof Component comp) {
                hoverTag.addTag("contents", serializeComponent(comp));
            } else if (hover.value() instanceof HoverEvent.ShowItem showItem) {
                NbtCompoundTag itemTag = new NbtCompoundTag();
                itemTag.addTag("id", NbtStringTag.from(showItem.item().asString()));
                itemTag.addTag("count", NbtStringTag.from(String.valueOf(showItem.count())));
                if (showItem.nbt() != null) {
                    itemTag.addTag("tag", NbtStringTag.from(showItem.nbt().string()));
                }
                hoverTag.addTag("contents", itemTag);
            } else if (hover.value() instanceof HoverEvent.ShowEntity showEntity) {
                NbtCompoundTag entityTag = new NbtCompoundTag();
                entityTag.addTag("type", NbtStringTag.from(showEntity.type().asString()));
                entityTag.addTag("id", NbtStringTag.from(showEntity.id().toString()));
                if (showEntity.name() != null) {
                    entityTag.addTag("name", serializeComponent(showEntity.name()));
                }
                hoverTag.addTag("contents", entityTag);
            }

            tag.addTag("hoverEvent", hoverTag);
        }
    }
}