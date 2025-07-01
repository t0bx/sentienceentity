package de.t0bx.sentienceEntity.packet.channel;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

public interface ChannelRegistry {
    public Channel getChannel(Player player);
}
