package de.t0bx.sentienceEntity.network.metadata;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MetadataEntry {
    public final int index;
    public final MetadataType type;
    public final Object value;
}
