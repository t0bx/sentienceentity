package de.t0bx.sentienceEntity.network.nbt;

import java.io.DataOutput;
import java.io.IOException;

public record NbtStringTag(String tag) implements NbtTag {

    @Override
    public byte getTagId() {
        return 8;
    }

    /**
     * Writes the string tag to the provided {@link DataOutput} stream.
     * The string content is written in UTF format.
     *
     * @param output the output stream to write the tag data
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public void write(DataOutput output) throws IOException {
        output.writeUTF(tag);
    }

    /**
     * Creates a new {@code NbtStringTag} instance with the specified string value.
     *
     * @param tag the string value to be encapsulated in the NbtStringTag
     * @return a new {@code NbtStringTag} instance containing the provided string value
     */
    public static NbtStringTag from(String tag) {
        return new NbtStringTag(tag);
    }
}
