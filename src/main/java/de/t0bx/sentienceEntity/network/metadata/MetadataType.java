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

    /**
     * Constructor for the MetadataType enum. Associates an identifier with a specific data-writing operation.
     *
     * @param id      The unique identifier for the metadata type.
     * @param writer  A BiConsumer responsible for writing the metadata value to the provided ByteBuf.
     */
    MetadataType(int id, BiConsumer<ByteBuf, Object> writer) {
        this.id = id;
        this.writer = writer;
    }

    /**
     * Writes a value to the provided {@link ByteBuf} using the associated writer
     * logic for the specific metadata type.
     *
     * @param buf the {@code ByteBuf} to which the value is written.
     * @param val the value to be written to the {@code ByteBuf}, its type must
     *            match the expected type of the associated metadata.
     */
    public void write(ByteBuf buf, Object val) {
        writer.accept(buf, val);
    }
}
