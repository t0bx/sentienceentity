package de.t0bx.sentienceEntity.network.inventory.equipment;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
public class Equipment {
    private final EquipmentSlot slot;
    private final ItemStack itemStack;
}
