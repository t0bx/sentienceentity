package de.t0bx.sentienceEntity.listener;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.npc.NpcsHandler;
import de.t0bx.sentienceEntity.npc.setup.NpcCreation;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static de.t0bx.sentienceEntity.utils.MessageUtils.sendMessage;

public class InventoryClickListener implements Listener {

    private final NamespacedKey spawnEggKey;
    private final NpcCreation npcCreation;
    private final NpcsHandler npcsHandler;
    private final MiniMessage miniMessage;
    private final String prefix;

    public InventoryClickListener(NpcCreation npcCreation, NpcsHandler npcsHandler) {
        this.spawnEggKey = new NamespacedKey("se", "spawn_egg");
        this.npcCreation = npcCreation;
        this.npcsHandler = npcsHandler;
        this.miniMessage = MiniMessage.miniMessage();
        this.prefix = SentienceEntity.getInstance().getPrefix();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null) return;

        if (event.getCurrentItem().getItemMeta() == null) return;

        if (event.getView().title().equals(miniMessage.deserialize("<dark_gray>» <green><b>Select Your Entity Type <dark_gray>«"))) {
            if (event.getSlot() == 45) {
                this.npcCreation.openInventory(player, 0);
                return;
            }

            if (event.getSlot() == 53) {
                this.npcCreation.openInventory(player, 1);
                return;
            }

            PersistentDataContainer container = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
            if (container.has(spawnEggKey)) {
                event.setCancelled(true);
                NpcCreation.NpcCreationBuilder builder = this.npcCreation.getCreationBuilder(player);

                EntityType type = EntityType.valueOf(container.get(spawnEggKey, PersistentDataType.STRING).toUpperCase());
                builder.setEntityType(type);
                if (type == EntityType.PLAYER) {
                    event.getView().close();
                    builder.nextStep();
                    sendMessage(player, miniMessage.deserialize(prefix + "Please type in the player name for the npc skin"));
                    return;
                }

                event.getView().close();
                builder.nextStep();
                builder.nextStep();
                sendMessage(player, miniMessage.deserialize(prefix + "Should this npc only be visible with a certain permission? (Type <red>none <gray>for no permission)"));
            }
        }
    }
}
