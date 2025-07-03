package de.t0bx.sentienceEntity.network.nbt;

import lombok.Getter;

import java.io.DataOutput;
import java.io.IOException;

public class NbtByteTag implements NbtTag {

    @Getter
    private final byte value;

    public NbtByteTag(byte value) {
        this.value = value;
    }

    @Override
    public byte getTagId() {
        return 1;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeByte(value);
    }
}
