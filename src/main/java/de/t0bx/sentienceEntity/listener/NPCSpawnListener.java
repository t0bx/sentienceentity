package de.t0bx.sentienceEntity.listener;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.npc.NPCsHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class NPCSpawnListener implements Listener {

    private final NPCsHandler npcsHandler;
    private final HologramManager hologramManager;

    public NPCSpawnListener() {
        this.npcsHandler = SentienceEntity.getInstance().getNpcshandler();
        this.hologramManager = SentienceEntity.getInstance().getHologramManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        this.npcsHandler.spawnAllNPCs(player);
        this.hologramManager.showAllHolograms(player);
    }

    @EventHandler
    public void onWorldSwitch(PlayerChangedWorldEvent event) {
        this.npcsHandler.despawnAllNPCs(event.getPlayer());
        this.npcsHandler.spawnAllNPCs(event.getPlayer());
    }
}
