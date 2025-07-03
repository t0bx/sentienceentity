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

    @Override
    public byte getTagId() {
        return 10;
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

    public void addTag(String name, NbtTag tag) {
        this.nbtTags.put(name, tag);
    }
}
