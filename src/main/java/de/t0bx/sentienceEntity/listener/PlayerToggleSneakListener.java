package de.t0bx.sentienceEntity.listener;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.npc.NPCsHandler;
import de.t0bx.sentienceEntity.npc.SentienceNPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerToggleSneakListener implements Listener {

    private final NPCsHandler npcsHandler;

    public PlayerToggleSneakListener() {
        this.npcsHandler = SentienceEntity.getInstance().getNpcshandler();
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        for (SentienceNPC npc : this.npcsHandler.getAllNPCs()) {
            if (npc.isShouldSneakWithPlayer()) {
                npc.updateSneaking(player);
            }
        }
    }
}
