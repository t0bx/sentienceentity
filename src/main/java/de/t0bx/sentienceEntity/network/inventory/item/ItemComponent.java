package de.t0bx.sentienceEntity.network.inventory.item;

import de.t0bx.sentienceEntity.network.nbt.NbtTag;

public class ItemComponent {
    public ComponentType type;
    public NbtTag data;

    public ItemComponent(ComponentType type, NbtTag data) {
        this.type = type;
        this.data = data;
    }
}
