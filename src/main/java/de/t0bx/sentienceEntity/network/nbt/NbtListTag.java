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

package de.t0bx.sentienceEntity.network.nbt;

import lombok.Getter;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class NbtListTag implements NbtTag {
    private final List<NbtTag> tags;

    public NbtListTag() {
        this.tags = new ArrayList<>();
    }

    /**
     * Retrieves the unique identifier for the List Tag type of NBT tag.
     *
     * @return a byte value representing the type identifier for the List Tag
     */
    @Override
    public byte getTagId() {
        return NbtTagIds.LIST_TAG.getId();
    }

    /**
     * Adds an {@code NbtTag} to the list of tags stored within this {@code NbtListTag}.
     *
     * @param tag the {@code NbtTag} to be added to this list
     */
    public void add(NbtTag tag) {
        tags.add(tag);
    }

    /**
     * Writes the serialized representation of this NBT list tag to the specified {@link DataOutput}.
     * The method writes the type identifier of the first child tag in the list (or 0 if the list is empty),
     * followed by the size of the list as an integer. Each child tag is then serialized in sequence.
     *
     * @param output the {@link DataOutput} stream to which the tag data will be written
     * @throws IOException if an I/O error occurs while writing to the output stream
     */
    @Override
    public void write(DataOutput output) throws IOException {
        byte typeId = tags.isEmpty() ? 0 : tags.get(0).getTagId();
        output.writeByte(typeId);
        output.writeInt(tags.size());

        for (NbtTag tag : tags) {
            tag.write(output);
        }
    }
}
