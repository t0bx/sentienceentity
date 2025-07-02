package de.t0bx.sentienceEntity.packet.nbt;

import java.io.DataOutput;
import java.io.IOException;

public interface NbtTag {
    byte getTagId();

    void write(DataOutput output) throws IOException;
}
