package de.t0bx.sentienceEntity.packet.utils;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MetadataEntry {
    public final int index;
    public final MetadataType type;
    public final Object value;
}
