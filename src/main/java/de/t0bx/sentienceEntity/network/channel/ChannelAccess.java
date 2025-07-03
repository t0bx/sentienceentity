package de.t0bx.sentienceEntity.network.channel;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

public class ChannelAccess {

    private static ChannelRegistry registry;

    public static void setRegistry(ChannelRegistry registry) {
        ChannelAccess.registry = registry;
    }

    /**
     * Retrieves the Netty channel associated with a specific player.
     *
     * @param player the player whose channel is to be retrieved
     * @return the Netty channel associated with the provided player
     * @throws IllegalStateException if the ChannelRegistry is not set
     */
    public static Channel getChannel(Player player) {
        if (registry == null) {
            throw new IllegalStateException("ChannelRegistry not set");
        }

        return registry.getChannel(player);
    }
}
