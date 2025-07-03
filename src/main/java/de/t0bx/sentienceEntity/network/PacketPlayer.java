package de.t0bx.sentienceEntity.network;

import de.t0bx.sentienceEntity.network.channel.ChannelAccess;
import de.t0bx.sentienceEntity.network.wrapper.PacketWrapper;
import io.netty.channel.Channel;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class PacketPlayer {
    private final Player player;
    private final Channel channel;

    public PacketPlayer(Player player) {
        this.player = player;
        this.channel = ChannelAccess.getChannel(player);
    }

    /**
     * Sends a packet to the associated player by writing and flushing it
     * through the player's channel.
     *
     * @param packet the {@link PacketWrapper} representing the packet to be sent.
     *               It contains the data to be sent over the network that is
     *               built using the {@code build()} method.
     */
    public void sendPacket(PacketWrapper packet) {
        channel.writeAndFlush(packet.build());
    }
}
