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
import java.util.HashMap;
import java.util.Map;

public class NbtCompoundTag implements NbtTag {

    @Getter
    private final Map<String, NbtTag> nbtTags;

    public NbtCompoundTag() {
        this.nbtTags = new HashMap<>();
    }

    /**
     * Retrieves the unique identifier for this NBT tag, representing its type.
     *
     * @return the byte value corresponding to the tag type of the compound tag
     */
    @Override
    public byte getTagId() {
        return NbtTagIds.COMPOUND_TAG.getId();
    }

    /**
     * Writes the NBT compound tag and its contained tags to the provided DataOutput stream.
     * Each tag is written sequentially, including its name, type, and content.
     * A terminating byte (value 0x00) is written at the end to signify the end of the compound tag.
     *
     * @param output the output stream to which the compound tag and its contents are written
     * @throws IOException if an I/O error occurs during the writing process
     */
    @Override
    public void write(DataOutput output) throws IOException {
        for (String tags : this.nbtTags.keySet()) {
            NbtTag tag = this.nbtTags.get(tags);
            writeTag(tags, tag, output);
        }

        output.writeByte(0x00);
    }

    /**
     * Writes an NBT tag with the given name, type, and data to the specified output.
     *
     * @param name the name of the tag to be written
     * @param nbtTag the NBT tag containing the data and type to be written
     * @param output the output stream to which the tag data is written
     * @throws IOException if an I/O error occurs during writing
     */
    public void writeTag(String name, NbtTag nbtTag, DataOutput output) throws IOException {
        output.writeByte(nbtTag.getTagId());
        if (nbtTag.getTagId() != 0) {
            output.writeUTF(name);
            nbtTag.write(output);
        }
    }

    /**
     * Adds a string tag to the NBT Compound Tag with the specified key and value.
     *
     * @param name the key under which the tag will be stored
     * @param tag the string value to be stored as the tag
     */
    public void addTag(String name, String tag) {
        this.nbtTags.put(name, NbtStringTag.from(tag));
    }

    /**
     * Checks if the specified key exists in the NBT compound tag.
     *
     * @param name the key to check for existence within the compound tag
     * @return true if the key exists, otherwise false
     */
    public boolean hasKey(String name) {
    	return this.nbtTags.containsKey(name);
    }

    /**
     * Adds an NBT tag to the compound tag using the specified key and value.
     *
     * @param name the key under which the tag will be stored
     * @param tag the NBT tag to be added to the compound tag
     */
    public void addTag(String name, NbtTag tag) {
        this.nbtTags.put(name, tag);
    }
}
