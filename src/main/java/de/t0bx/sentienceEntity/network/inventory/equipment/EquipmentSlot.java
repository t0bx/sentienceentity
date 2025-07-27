package de.t0bx.sentienceEntity.network.inventory.equipment;

import lombok.Getter;

@Getter
public enum EquipmentSlot {
    MAIN_HAND(0),
    OFF_HAND(1),
    BOOTS(2),
    LEGGINGS(3),
    CHEST_PLATE(4),
    HELMET(5);

    private final int id;

    EquipmentSlot(int id) {
        this.id = id;
    }
}
