package de.t0bx.sentienceEntity.network.nbt;

import java.io.DataOutput;
import java.io.IOException;

public class NbtDoubleTag implements NbtTag {
    private final double value;

    public NbtDoubleTag(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public byte getTagId() {
        return 6;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeDouble(value);
    }
}
