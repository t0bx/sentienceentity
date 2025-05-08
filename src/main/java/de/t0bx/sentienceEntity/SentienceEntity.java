package de.t0bx.sentienceEntity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import de.t0bx.sentienceEntity.commands.SentienceEntityCommand;
import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.listener.NPCSpawnListener;
import de.t0bx.sentienceEntity.listener.PlayerMoveListener;
import de.t0bx.sentienceEntity.listener.PlayerToggleSneakListener;
import de.t0bx.sentienceEntity.npc.NPCsHandler;
import de.t0bx.sentienceEntity.packetlistener.PacketReceiveListener;
import de.t0bx.sentienceEntity.utils.SkinFetcher;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class SentienceEntity extends JavaPlugin {

    @Getter
    private static SentienceEntity instance;

    private final String prefix = "<gradient:#0a0f2c:#0f4a6b:#00cfff><bold>SentienceEntity</bold></gradient> <dark_gray>| <gray>";

    private SkinFetcher skinFetcher;

    private NPCsHandler npcshandler;

    private HologramManager hologramManager;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        instance = this;
        this.getLogger().info("Starting SentienceEntity...");
        PacketEvents.getAPI().init();

        this.skinFetcher = new SkinFetcher(this);
        this.npcshandler = new NPCsHandler();
        this.hologramManager = new HologramManager();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketReceiveListener(this.npcshandler), PacketListenerPriority.NORMAL);
        this.getLogger().info("Loaded " + this.npcshandler.getLoadedSize() + " NPCs.");

        Bukkit.getPluginManager().registerEvents(new NPCSpawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerToggleSneakListener(), this);
        this.getCommand("se").setExecutor(new SentienceEntityCommand(this));

        this.getLogger().info("SentienceEntity has been enabled!");
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        this.hologramManager.destroyAll();
    }
}
