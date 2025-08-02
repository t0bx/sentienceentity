package de.t0bx.sentienceEntity.npc.setup;

import de.t0bx.sentienceEntity.inventory.InventoryProvider;
import de.t0bx.sentienceEntity.network.version.registries.ItemIdRegistry;
import de.t0bx.sentienceEntity.utils.item.ItemProvider;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class NpcCreation {

    private final Map<Player, NpcCreationBuilder> builderMap;
    private final InventoryProvider inventoryProvider;
    private static final int ITEMS_PER_PAGE = 44;

    public NpcCreation(InventoryProvider inventoryProvider) {
        this.builderMap = new ConcurrentHashMap<>();
        this.inventoryProvider = inventoryProvider;
    }

    public boolean isNpcCreation(Player player) {
        return builderMap.containsKey(player);
    }

    public void openInventory(Player player, int page) {
        List<Material> spawnEggs = ItemIdRegistry.getSpawnEggs();
        int totalPages = (int) Math.ceil(spawnEggs.size() / (double) ITEMS_PER_PAGE);

        Inventory inventory = this.inventoryProvider.getInventory(
                player,
                "create_npc",
                6 * 9,
                "<dark_gray>» <green><b>Select Your Entity Type <dark_gray>«"
        );
        inventory.clear();

        if (page == 0) {
            inventory.setItem(0, new ItemProvider(Material.PLAYER_HEAD).setName("<gray>Player").setPersistentData("se", "spawn_egg", "player").build());
        }

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, spawnEggs.size());
        int slot = (page == 0 ? 1 : 0);

        for (int i = start; i < end; i++) {
            Material spawnEgg = spawnEggs.get(i);
            String name = spawnEgg.name().replaceAll("_SPAWN_EGG$", "").toLowerCase();

            inventory.setItem(slot++, new ItemProvider(spawnEgg)
                    .setPersistentData("se", "spawn_egg", name)
                    .build());
        }

        if (page > 0) {
            inventory.setItem(45, new ItemProvider(Material.ARROW).setName("<gray>← Previous Page").build());
        }

        if (page < totalPages - 1) {
            inventory.setItem(53, new ItemProvider(Material.ARROW).setName("<gray>Next Page →").build());
        }

        player.openInventory(inventory);
    }

    public NpcCreationBuilder getCreationBuilder(Player player) {
        return builderMap.get(player);
    }

    public void removeCreationBuilder(Player player) {
        builderMap.remove(player);
    }

    public void addCreationBuilder(Player player) {
        builderMap.put(player, new NpcCreationBuilder());
    }

    @Data
    public static class NpcCreationBuilder {
        private String name;
        private EntityType entityType;
        private @Nullable String playerName;
        private @Nullable String permission;
        private SetupStep step = SetupStep.NAME;

        public SetupStep nextStep() {
            return switch (step) {
                case NAME -> step = SetupStep.ENTITY_TYPE;
                case ENTITY_TYPE -> step = SetupStep.PLAYER_NAME;
                case PLAYER_NAME -> step = SetupStep.PERMISSION;
                case PERMISSION -> step = SetupStep.DONE;
                default -> SetupStep.DONE;
            };
        }
    }

    public static enum SetupStep {
        NAME,
        ENTITY_TYPE,
        PLAYER_NAME,
        PERMISSION,
        DONE;
    }
}
