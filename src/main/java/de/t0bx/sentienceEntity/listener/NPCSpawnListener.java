/**
 *Creative Commons Attribution-NonCommercial 4.0 International Public License
 * By using this code, you agree to the following terms:
 * You are free to:
 * - Share — copy and redistribute the material in any medium or format
 * - Adapt — remix, transform, and build upon the material
 * Under the following terms:
 * 1. Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made.
 * 2. NonCommercial — You may not use the material for commercial purposes.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 * Full License Text: https://creativecommons.org/licenses/by-nc/4.0/legalcode
 * ---
 * Copyright (c) 2025 t0bx
 * This work is licensed under the Creative Commons Attribution-NonCommercial 4.0 International License.
 */

package de.t0bx.sentienceEntity.listener;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.npc.NPCsHandler;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;

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
        SentienceEntity.getInstance().getPacketInterceptor().injectPlayer(player);

        this.npcsHandler.spawnAllNPCs(player);
        this.hologramManager.showAllHolograms(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        SentienceEntity.getInstance().getPacketInterceptor().uninjectPlayer(player);
    }

    @EventHandler
    public void onWorldSwitch(PlayerChangedWorldEvent event) {
        this.npcsHandler.despawnAllNPCs(event.getPlayer());
        this.npcsHandler.spawnAllNPCs(event.getPlayer());
    }

    private void debugConnection(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        try {
            System.out.println("ServerPlayer class: " + serverPlayer.getClass().getName());
            System.out.println("Connection class: " + serverPlayer.connection.getClass().getName());

            Field[] fields = serverPlayer.connection.getClass().getDeclaredFields();
            for (Field field : fields) {
                System.out.println("Field: " + field.getName() + " - Type: " + field.getType().getName());
            }

            Class<?> superClass = serverPlayer.connection.getClass().getSuperclass();
            while (superClass != null) {
                System.out.println("SuperClass: " + superClass.getName());
                Field[] superFields = superClass.getDeclaredFields();
                for (Field field : superFields) {
                    System.out.println("Field: " + field.getName() + " - Type: " + field.getType().getName());
                }
                superClass = superClass.getSuperclass();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
