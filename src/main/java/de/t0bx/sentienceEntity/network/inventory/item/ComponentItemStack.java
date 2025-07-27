package de.t0bx.sentienceEntity.network.inventory.item;

import de.t0bx.sentienceEntity.network.nbt.*;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class ComponentItemStack {

    private int count;
    private int id;
    private final List<ItemComponent> components = new ArrayList<>();

    public static ComponentItemStack fromBukkit(ItemStack itemStack, int itemId) {
        ComponentItemStack componentItemStack = new ComponentItemStack();

        componentItemStack.count = itemStack.getAmount();
        componentItemStack.id = itemId;

        NbtCompoundTag rootTag = new NbtCompoundTag();
        rootTag.addTag("Slot", new NbtByteTag((byte) 0));
        rootTag.addTag("id", new NbtStringTag("minecraft:" + itemStack.getType().name().toLowerCase()));
        rootTag.addTag("count", new NbtIntTag(itemStack.getAmount()));

        if (itemStack.hasData(DataComponentTypes.CUSTOM_MODEL_DATA)) {
            CustomModelData customModelData = itemStack.getData(DataComponentTypes.CUSTOM_MODEL_DATA);
            NbtCompoundTag compoundTag = new NbtCompoundTag();

            NbtListTag floatList = new NbtListTag();
            for (Float value : customModelData.floats()) {
                floatList.add(new NbtFloatTag(value));
            }
            compoundTag.addTag("floats", floatList);

            NbtListTag flagsList = new NbtListTag();
            for (Boolean value : customModelData.flags()) {
                flagsList.add(new NbtByteTag(value ? (byte) 1 : (byte) 0));
            }
            compoundTag.addTag("flags", flagsList);

            NbtListTag stringList = new NbtListTag();
            for (String value : customModelData.strings()) {
                stringList.add(new NbtStringTag(value));
            }
            compoundTag.addTag("strings", stringList);

            NbtListTag colorList = new NbtListTag();
            for (Color value : customModelData.colors()) {
                colorList.add(new NbtIntTag(value.asRGB()));
            }

            compoundTag.addTag("colors", colorList);

            rootTag.addTag("minecraft:custom_model_data", compoundTag);
            componentItemStack.components.add(new ItemComponent(ComponentType.CUSTOM_MODEL_DATA, compoundTag));
        }

        if (itemStack.hasData(DataComponentTypes.ENCHANTMENTS)) {
            ItemEnchantments enchantment = itemStack.getData(DataComponentTypes.ENCHANTMENTS);

            NbtCompoundTag compoundTag = new NbtCompoundTag();

            for (Map.Entry<Enchantment, Integer> entry : enchantment.enchantments().entrySet()) {
                Enchantment enchantment1 = entry.getKey();
                int level = entry.getValue();

                String enchantmentId = enchantment1.getKey().toString();

                compoundTag.addTag(enchantmentId, new NbtIntTag(level));
            }

            rootTag.addTag("minecraft:enchantments", compoundTag);
            componentItemStack.components.add(new ItemComponent(ComponentType.ENCHANTMENTS, compoundTag));
        }

        if (itemStack.hasData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE)) {
            boolean value = itemStack.getData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);

            NbtByteTag byteTag = new NbtByteTag(value ? (byte) 1 : (byte) 0);
            rootTag.addTag("minecraft:enchantment_glint_override", byteTag);

            componentItemStack.components.add(new ItemComponent(
                    ComponentType.ENCHANTMENT_GLINT_OVERRIDE, byteTag
            ));
        }

        if (itemStack.hasData(DataComponentTypes.EQUIPPABLE)) {
            Equippable equippable = itemStack.getData(DataComponentTypes.EQUIPPABLE);

            NbtCompoundTag compoundTag = new NbtCompoundTag();

            compoundTag.addTag("slot", new NbtStringTag(equippable.slot().name()));
            compoundTag.addTag("equip_sound", new NbtStringTag(equippable.equipSound().value()));
            compoundTag.addTag("asset_id", new NbtStringTag(equippable.assetId().value()));

            if (equippable.allowedEntities() != null && !equippable.allowedEntities().isEmpty()) {
                NbtListTag entityList = new NbtListTag();
                for (String entityId : equippable.allowedEntities().values().stream().map(Key::value).toList()) {
                    entityList.add(new NbtStringTag(entityId));
                }
                compoundTag.addTag("allowed_entities", entityList);
            }

            compoundTag.addTag("dispensable", new NbtByteTag(equippable.dispensable() ? (byte) 1 : (byte) 0));
            compoundTag.addTag("swappable", new NbtByteTag(equippable.swappable() ? (byte) 1 : (byte) 0));
            compoundTag.addTag("damage_on_hurt", new NbtByteTag(equippable.damageOnHurt() ? (byte) 1 : (byte) 0));
            compoundTag.addTag("equip_on_interact", new NbtByteTag(equippable.equipOnInteract() ? (byte) 1 : (byte) 0));

            if (equippable.cameraOverlay() != null && !equippable.cameraOverlay().value().isEmpty()) {
                compoundTag.addTag("camera_overlay", new NbtStringTag(equippable.cameraOverlay().value()));
            }

            rootTag.addTag("minecraft:equippable", compoundTag);
            componentItemStack.components.add(new ItemComponent(ComponentType.EQUIPPABLE, compoundTag));
        }

        if (itemStack.hasData(DataComponentTypes.ITEM_MODEL)) {
            Key itemModel = itemStack.getData(DataComponentTypes.ITEM_MODEL);

            NbtStringTag stringTag = new NbtStringTag(itemModel.value());
            rootTag.addTag("minecraft:item_model", stringTag);

            componentItemStack.components.add(new ItemComponent(ComponentType.ITEM_MODEL, stringTag));
        }

        return componentItemStack;
    }
}
