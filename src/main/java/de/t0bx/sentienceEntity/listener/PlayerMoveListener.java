package de.t0bx.sentienceEntity.listener;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.npc.NPCsHandler;
import de.t0bx.sentienceEntity.npc.SentienceNPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final NPCsHandler npcsHandler;

    public PlayerMoveListener() {
        this.npcsHandler = SentienceEntity.getInstance().getNpcshandler();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        for (SentienceNPC npc : this.npcsHandler.getAllNPCs()) {
            if (npc.isShouldLookAtPlayer()) {
                npc.updateLookingAtPlayer(player);
            }
        }
    }
}
