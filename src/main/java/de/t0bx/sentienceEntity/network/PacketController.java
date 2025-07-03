package de.t0bx.sentienceEntity.network;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketController {
    private final Map<Player, PacketPlayer> cachedChannels = new ConcurrentHashMap<>();
    private final Map<Channel, Player> mappedChannels = new ConcurrentHashMap<>();

    /**
     * Retrieves the {@link PacketPlayer} instance associated with the specified {@link Player}.
     * If a {@link PacketPlayer} is already cached for the given player, it returns the cached instance.
     * Otherwise, a new {@link PacketPlayer} is created, cached, and returned.
     *
     * @param player the {@link Player} for whom the {@link PacketPlayer} is to be retrieved.
     * @return the {@link PacketPlayer} instance associated with the specified {@link Player}.
     */
    public PacketPlayer getPlayer(Player player) {
        if (cachedChannels.containsKey(player)) {
            return cachedChannels.get(player);
        }

        PacketPlayer packetPlayer = new PacketPlayer(player);
        cachedChannels.put(player, packetPlayer);
        mappedChannels.put(packetPlayer.getChannel(), player);
        return packetPlayer;
    }

    public Player getPlayer(Channel channel) {
        return mappedChannels.getOrDefault(channel, null);
    }

    public void removePlayer(Player player) {
        PacketPlayer packetPlayer = this.cachedChannels.remove(player);
        if (packetPlayer != null) {
            this.mappedChannels.remove(packetPlayer.getChannel());
        }
    }
}
