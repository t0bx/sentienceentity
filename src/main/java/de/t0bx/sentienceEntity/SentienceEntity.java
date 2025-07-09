/**
 SentienceEntity API License v1.1
 Copyright (c) 2025 (t0bx)

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to use, copy, modify, and integrate the Software into their own projects, including commercial and closed-source projects, subject to the following conditions:

 1. Attribution:
 You must give appropriate credit to the original author ("Tobias Schuster" or "t0bx"), provide a link to the source or official page if available, and indicate if changes were made. You must do so in a reasonable and visible manner, such as in your plugin.yml, README, or about page.

 2. No Redistribution or Resale:
 You may NOT sell, redistribute, or otherwise make the original Software or modified standalone versions of it available as a product (free or paid), plugin, or downloadable file, unless you have received prior written permission from the author. This includes publishing the plugin on any marketplace (e.g., SpigotMC, MC-Market, Polymart) or including it in paid bundles.

 3. Use as Dependency/API:
 You are allowed to use this Software as a dependency or library in your own plugin or project, including in paid products, as long as attribution is given and the Software itself is not being sold or published separately.

 4. No Misrepresentation:
 You may not misrepresent the origin of the Software. You must clearly distinguish your own modifications from the original work. The original author's name may not be removed from the source files or documentation.

 5. License Retention:
 This license notice and all conditions must be preserved in all copies or substantial portions of the Software.

 6. Disclaimer:
 THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY ARISING FROM THE USE OF THIS SOFTWARE.

 ---

 Summary (non-binding):
 You may use this plugin in your projects, even commercially, but you may not resell or republish it. Always give credit to t0bx.
 */

package de.t0bx.sentienceEntity;

import de.t0bx.sentienceEntity.commands.SentienceEntityCommand;
import de.t0bx.sentienceEntity.config.ConfigFileManager;
import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.listener.NpcSpawnListener;
import de.t0bx.sentienceEntity.listener.PlayerMoveListener;
import de.t0bx.sentienceEntity.listener.PlayerToggleSneakListener;
import de.t0bx.sentienceEntity.npc.NpcsHandler;
import de.t0bx.sentienceEntity.network.PacketController;
import de.t0bx.sentienceEntity.network.channel.ChannelAccess;
import de.t0bx.sentienceEntity.network.channel.PaperChannelAccess;
import de.t0bx.sentienceEntity.network.channel.SpigotChannelAccess;
import de.t0bx.sentienceEntity.network.handler.PacketReceiveHandler;
import de.t0bx.sentienceEntity.update.UpdateManager;
import de.t0bx.sentienceEntity.utils.SkinFetcher;
import lombok.Getter;
import lombok.Setter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

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

    private PacketReceiveHandler packetReceiveHandler;

    @Getter
    private static SentienceAPI api;

    private boolean PAPER;

    private ConfigFileManager configFileManager;

    @Setter
    private boolean bStatsEnabled;

    private Metrics metrics;

    @Override
    public void onLoad() {
        if (isPaperServer()) {
            PAPER = true;
            ChannelAccess.setRegistry(new PaperChannelAccess());
            this.getLogger().info("SentienceEntity using PaperChannelAccess");
        } else {
            PAPER = false;
            ChannelAccess.setRegistry(new SpigotChannelAccess());
            this.getLogger().info("SentienceEntity using SpigotChannelAccess");
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        this.getLogger().info("Starting SentienceEntity...");
        this.configFileManager = new ConfigFileManager();

        if (this.bStatsEnabled) {
            this.getLogger().info("bStats is enabled for SentienceEntity.");
            int bStatsPluginId = 26431;
            this.metrics = new Metrics(this, bStatsPluginId);
        } else {
            this.getLogger().info("bStats is disabled for SentienceEntity.");
        }

        this.updateManager = new UpdateManager(this);
        this.updateManager.checkForUpdate();

        this.skinFetcher = new SkinFetcher(this);

        this.packetController = new PacketController();

        this.npcshandler = new NpcsHandler();
        this.hologramManager = new HologramManager();
        this.packetReceiveHandler = new PacketReceiveHandler(this.npcshandler, this.packetController);
        this.getLogger().info("Loaded " + this.npcshandler.getLoadedSize() + " NPCs.");

        Bukkit.getPluginManager().registerEvents(new NpcSpawnListener(), this);
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
        if (this.npcshandler != null) {
            this.npcshandler.despawnAll();
        }

        for (Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            if (team.getName().startsWith("hidden_")) {
                team.unregister();
            }
        }

        if (bStatsEnabled) {
            if (this.metrics != null) {
                this.metrics.shutdown();
            }
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
