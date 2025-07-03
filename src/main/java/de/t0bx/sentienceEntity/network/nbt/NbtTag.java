package de.t0bx.sentienceEntity.network.nbt;

import java.io.DataOutput;
import java.io.IOException;

public interface NbtTag {
    byte getTagId();

    void write(DataOutput output) throws IOException;
}
