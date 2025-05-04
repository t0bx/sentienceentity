package de.t0bx.sentienceEntity;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class SentienceEntity extends JavaPlugin {

    @Getter
    private static SentienceEntity instance;

    private final String prefix = "<gradient:#0a0f2c:#0f4a6b:#00cfff><bold>SentienceEntity</bold></gradient> <dark_gray>| <gray>";

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();

    }

    @Override
    public void onEnable() {
        instance = this;
        this.getLogger().info("Starting Sentience Plugin...");
        PacketEvents.getAPI().init();

    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();

    }
}
