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

package de.t0bx.sentienceEntity;

import de.t0bx.sentienceEntity.commands.SentienceEntityCommand;
import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.listener.NPCSpawnListener;
import de.t0bx.sentienceEntity.listener.PlayerMoveListener;
import de.t0bx.sentienceEntity.listener.PlayerToggleSneakListener;
import de.t0bx.sentienceEntity.npc.NpcsHandler;
import de.t0bx.sentienceEntity.packet.PacketController;
import de.t0bx.sentienceEntity.packet.channel.ChannelAccess;
import de.t0bx.sentienceEntity.packet.channel.PaperChannelAccess;
import de.t0bx.sentienceEntity.packet.channel.SpigotChannelAccess;
import de.t0bx.sentienceEntity.packets.PacketInterceptor;
import de.t0bx.sentienceEntity.update.UpdateManager;
import de.t0bx.sentienceEntity.utils.SkinFetcher;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class SentienceEntity extends JavaPlugin {

    @Getter
    private static SentienceEntity instance;

    private UpdateManager updateManager;

    private final String prefix = "<gradient:#0a0f2c:#0f4a6b:#00cfff><bold>SentienceEntity</bold></gradient> <dark_gray>| <gray>";

    private SkinFetcher skinFetcher;

    private PacketController packetController;

    private NpcsHandler npcshandler;

    private HologramManager hologramManager;

    private PacketInterceptor packetInterceptor;

    @Getter
    private static SentienceAPI api;

    @Override
    public void onLoad() {
        if (isPaperServer()) {
            ChannelAccess.setRegistry(new PaperChannelAccess());
            this.getLogger().info("SentienceEntity using PaperChannelAccess");
        } else {
            ChannelAccess.setRegistry(new SpigotChannelAccess());
            this.getLogger().info("SentienceEntity using SpigotChannelAccess");
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        this.getLogger().info("Starting SentienceEntity...");

        this.updateManager = new UpdateManager(this);
        this.updateManager.checkForUpdate();

        this.skinFetcher = new SkinFetcher(this);

        this.packetController = new PacketController();

        this.npcshandler = new NpcsHandler();
        this.hologramManager = new HologramManager();
        this.packetInterceptor = new PacketInterceptor(this.npcshandler);
        this.getLogger().info("Loaded " + this.npcshandler.getLoadedSize() + " NPCs.");

        Bukkit.getPluginManager().registerEvents(new NPCSpawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerToggleSneakListener(), this);
        this.getCommand("se").setExecutor(new SentienceEntityCommand(this));

        api = new SentienceAPI();
        this.getLogger().info("SentienceEntity has been enabled!");
    }

    @Override
    public void onDisable() {
        if (this.hologramManager != null) {
            this.hologramManager.destroyAll();
        }
    }

    private boolean isPaperServer() {
        try {
            Class.forName("com.destroystokyo.paper.utils.PaperPluginLogger");
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }
}
