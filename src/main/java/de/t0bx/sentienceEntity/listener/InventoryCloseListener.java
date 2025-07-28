package de.t0bx.sentienceEntity.listener;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.npc.setup.NpcCreation;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import static de.t0bx.sentienceEntity.utils.MessageUtils.sendMessage;

public class InventoryCloseListener implements Listener {

    private final NpcCreation npcCreation;

    public InventoryCloseListener(NpcCreation npcCreation) {
        this.npcCreation = npcCreation;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (event.getReason() == InventoryCloseEvent.Reason.PLAYER) {
            if (this.npcCreation.isNpcCreation(player)) {
                this.npcCreation.removeCreationBuilder(player);
                sendMessage(player, MiniMessage.miniMessage().deserialize(SentienceEntity.getInstance().getPrefix() + "<red>Cancelled the npc creation"));
            }
        }
    }
}
