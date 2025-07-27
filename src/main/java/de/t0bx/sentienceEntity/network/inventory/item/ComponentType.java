package de.t0bx.sentienceEntity.network.inventory.item;

import lombok.Getter;

@Getter
public enum ComponentType {
    CUSTOM_MODEL_DATA(14, "minecraft:custom_model_data"),
    ENCHANTMENTS(10, "minecraft:enchantments"),
    ENCHANTMENT_GLINT_OVERRIDE(18, "minecraft:enchantment_glint_override"),
    EQUIPPABLE(28, "minecraft:equippable"),
    ITEM_MODEL(7, "minecraft:item_model");


    private final int id;
    private final String name;

    ComponentType(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
