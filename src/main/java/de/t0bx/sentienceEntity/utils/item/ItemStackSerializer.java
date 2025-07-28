package de.t0bx.sentienceEntity.utils.item;

import de.t0bx.sentienceEntity.SentienceEntity;
import org.bukkit.inventory.ItemStack;
import java.util.Base64;
import java.util.logging.Level;

public class ItemStackSerializer {

    public static String serializeItemStack(ItemStack itemStack) {
        if (itemStack == null) return null;
        return Base64.getEncoder().encodeToString(itemStack.serializeAsBytes());
    }

    public static ItemStack deserializeItemStack(String serialized) {
        if (serialized == null || serialized.isEmpty()) return null;

        try {
            byte[] bytes = Base64.getDecoder().decode(serialized);
            return ItemStack.deserializeBytes(bytes);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            SentienceEntity.getInstance().getLogger().log(Level.WARNING, "Failed to deserialize ItemStack: ", exception);
            return null;
        }
    }
}
