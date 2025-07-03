package de.t0bx.sentienceEntity.network.nbt;

import lombok.Getter;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NbtListTag implements NbtTag {
    @Getter
    private final List<NbtTag> tags = new ArrayList<>();

    @Override
    public byte getTagId() {
        return 9;
    }

    public void add(NbtTag tag) {
        tags.add(tag);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        byte typeId = tags.isEmpty() ? 0 : tags.getFirst().getTagId();
        output.writeByte(typeId);
        output.writeInt(tags.size());

        for (NbtTag tag : tags) {
            tag.write(output);
        }
    }
}
