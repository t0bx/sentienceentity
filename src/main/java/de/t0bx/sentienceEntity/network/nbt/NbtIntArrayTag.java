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

import java.io.DataOutput;
import java.io.IOException;

/**
 * Represents an NBT (Named Binary Tag) tag for storing an array of integers. This class is a record
 * that encapsulates an array of integers and implements the {@code NbtTag} interface to facilitate
 * serialization of the integer array in compliance with the NBT format.
 *
 * Instances of this record hold an array of integers as their value and provide methods to retrieve
 * the tag's type identifier and write the tag's data to a {@code DataOutput} stream.
 */
public record NbtIntArrayTag(int[] values) implements NbtTag{

    /**
     * Retrieves the unique identifier for the INT_ARRAY_TAG type of NBT tag.
     *
     * @return a byte value representing the type identifier for the INT_ARRAY_TAG
     */
    @Override
    public byte getTagId() {
        return NbtTagIds.INT_ARRAY_TAG.getId();
    }

    /**
     * Writes the data encapsulated within this NBT tag to the specified {@link DataOutput} stream,
     * including the tag type and the array of integers.
     *
     * The output starts with the length of the integer array, followed by each integer value in
     * the array being written sequentially.
     *
     * @param output the {@code DataOutput} stream to which the integer array data will be written
     * @throws IOException if an I/O error occurs while writing to the stream
     */
    @Override
    public void write(DataOutput output) throws IOException {
        output.writeInt(values.length);
        for (int i : values) {
            output.writeInt(i);
        }
    }
}
