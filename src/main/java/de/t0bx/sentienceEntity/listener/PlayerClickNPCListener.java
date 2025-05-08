package de.t0bx.sentienceEntity.listener;

import de.t0bx.sentienceEntity.events.PlayerClickNPCEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerClickNPCListener implements Listener {

    @EventHandler
    public void onPlayerClickNPC(PlayerClickNPCEvent event) {
        Player player = event.getPlayer();

        player.sendMessage(MiniMessage.miniMessage().deserialize("Npcname -> " + event.getNpcName() + " -> " + event.getClickType().name()));
    }
}
