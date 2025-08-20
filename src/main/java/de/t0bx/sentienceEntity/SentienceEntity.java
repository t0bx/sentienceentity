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
import de.t0bx.sentienceEntity.commands.SentienceHologramCommand;
import de.t0bx.sentienceEntity.commands.SentiencePathCommand;
import de.t0bx.sentienceEntity.config.ConfigFileManager;
import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.inventory.InventoryProvider;
import de.t0bx.sentienceEntity.listener.*;
import de.t0bx.sentienceEntity.network.PacketController;
import de.t0bx.sentienceEntity.network.channel.ChannelAccess;
import de.t0bx.sentienceEntity.network.channel.PaperChannelAccess;
import de.t0bx.sentienceEntity.network.channel.SpigotChannelAccess;
import de.t0bx.sentienceEntity.network.handler.PacketReceiveHandler;
import de.t0bx.sentienceEntity.npc.NpcsHandler;
import de.t0bx.sentienceEntity.npc.SentienceNPC;
import de.t0bx.sentienceEntity.npc.setup.NpcCreation;
import de.t0bx.sentienceEntity.path.SentiencePathHandler;
import de.t0bx.sentienceEntity.path.data.SentiencePathType;
import de.t0bx.sentienceEntity.update.UpdateManager;
import de.t0bx.sentienceEntity.utils.SkinFetcher;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Getter
public final class SentienceEntity extends JavaPlugin {

    @Getter
    private static SentienceEntity instance;

    @Setter
    private boolean paper;

    private ConfigFileManager configFileManager;

    @Setter
    private boolean bStatsEnabled;

    private Metrics metrics;

    private UpdateManager updateManager;

    private final String prefix = "<gradient:#0a0f2c:#0f4a6b:#00cfff>SentienceEntity</gradient> <dark_gray>| <gray>";

    private SkinFetcher skinFetcher;
    private PacketController packetController;
    private InventoryProvider inventoryProvider;
    private NpcCreation npcCreation;
    private NpcsHandler npcshandler;
    private HologramManager hologramManager;
    private SentiencePathHandler sentiencePathHandler;
    private PacketReceiveHandler packetReceiveHandler;

    @Getter
    private static SentienceAPI api;

    private BukkitAudiences audiences;

    private final List<Player> inspectList = new ArrayList<>();

    @Override
    public void onLoad() {
        this.setPaper(isPaperServer());

        ChannelAccess.setRegistry(isPaper() ? new PaperChannelAccess() : new SpigotChannelAccess());
        String message = isPaper() ? "SentienceEntity is running on Paper!" : "SentienceEntity is running on Spigot!";
        this.getLogger().info(message);
    }

    @Override
    public void onEnable() {
        instance = this;
        this.getLogger().info("Starting SentienceEntity...");
        this.configFileManager = new ConfigFileManager();

        if (this.bStatsEnabled) {
            this.getLogger().info("bStats is enabled for SentienceEntity.");
            this.metrics = new Metrics(this, 26431);
        } else {
            this.getLogger().info("bStats is disabled for SentienceEntity.");
        }

        if (!isPaper()) this.audiences = BukkitAudiences.create(this);

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
           try {
               Class.forName("de.t0bx.sentienceEntity.network.version.registries.ItemIdRegistry");
               Class.forName("de.t0bx.sentienceEntity.network.version.registries.EntityTypeRegistry");
               Class.forName("de.t0bx.sentienceEntity.boundingbox.BoundingBoxRegistry");

               getLogger().info("Loaded Registries.");
           } catch (Exception exception) {
               getLogger().log(Level.SEVERE, "Error while loading Registries!", exception);
               getLogger().warning("Disabling plugin...");
               Bukkit.getPluginManager().disablePlugin(this);
           }
        });

        this.updateManager = new UpdateManager(this);
        this.updateManager.checkForUpdate();

        this.skinFetcher = new SkinFetcher(this);

        this.packetController = new PacketController();

        this.inventoryProvider = new InventoryProvider();
        this.npcCreation = new NpcCreation(this.inventoryProvider);
        this.npcshandler = new NpcsHandler();
        this.hologramManager = new HologramManager();
        this.sentiencePathHandler = new SentiencePathHandler();

        this.packetReceiveHandler = new PacketReceiveHandler(this.npcshandler, this.packetController);
        this.getLogger().info("Loaded " + this.npcshandler.getLoadedSize() + " NPCs.");

        final PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new NpcSpawnListener(), this);
        pluginManager.registerEvents(new PlayerMoveListener(), this);
        pluginManager.registerEvents(new PlayerToggleSneakListener(), this);
        pluginManager.registerEvents(new AsyncPlayerChatListener(this.npcCreation, this.npcshandler), this);
        pluginManager.registerEvents(new InventoryClickListener(this.npcCreation, this.npcshandler), this);
        pluginManager.registerEvents(new InventoryCloseListener(this.npcCreation), this);
        pluginManager.registerEvents(new PlayerClickNpcListener(this), this);

        this.getCommand("se").setExecutor(new SentienceEntityCommand(this));
        this.getCommand("sp").setExecutor(new SentiencePathCommand(this));
        this.getCommand("sh").setExecutor(new SentienceHologramCommand(this));

        api = new SentienceAPI();
        this.getLogger().info("SentienceEntity has been enabled!");

        for (SentienceNPC npc : npcshandler.getAllNPCs()) {
            String pathName = npc.getBoundedPathName();
            if (pathName == null) continue;
            if (sentiencePathHandler.getPath(pathName).getType() != SentiencePathType.LOOP) continue;

            sentiencePathHandler.applyPath(npc.getEntityId(), pathName);
        }
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

        if (this.audiences != null) {
            this.audiences.close();
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
