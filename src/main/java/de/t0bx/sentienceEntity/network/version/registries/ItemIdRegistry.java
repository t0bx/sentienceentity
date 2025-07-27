package de.t0bx.sentienceEntity.network.version.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.network.version.VersionRegistry;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemIdRegistry {
    private static final Map<String, Integer> itemIds = new HashMap<>();
    @Getter
    private static final List<Material> spawnEggs = new ArrayList<>();

    static {
        String version = VersionRegistry.getVersion().getVersionString();
        InputStream inputStream = SentienceEntity.getInstance().getClass().getResourceAsStream("/registries/" + version + ".json");
        if (inputStream == null) throw new RuntimeException("Could not find registry for version " + version);

        JsonObject root = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();

        JsonObject items = root.getAsJsonObject("minecraft:item").getAsJsonObject("entries");
        for (Map.Entry<String, JsonElement> entry : items.entrySet()) {
            String key = entry.getKey();
            int id = entry.getValue().getAsJsonObject().get("protocol_id").getAsInt();
            if (key.endsWith("_spawn_egg")) {
                spawnEggs.add(Material.valueOf(key.substring(key.indexOf(":") + 1).toUpperCase()));
            }

            itemIds.put(key, id);
        }
    }

    public static int getItemId(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return 0;
        }

        String itemName = "minecraft:" + itemStack.getType().name().toLowerCase();

        Integer id = itemIds.get(itemName);
        if (id != null) return id;

        SentienceEntity.getInstance().getLogger().warning("Could not find item id for " + itemName);
        return 0;
    }
}
