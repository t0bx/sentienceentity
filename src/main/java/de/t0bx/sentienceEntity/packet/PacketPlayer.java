package de.t0bx.sentienceEntity.packet;

import de.t0bx.sentienceEntity.packet.channel.ChannelAccess;
import de.t0bx.sentienceEntity.packet.wrapper.PacketWrapper;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;

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
