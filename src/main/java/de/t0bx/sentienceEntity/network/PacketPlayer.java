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

    /**
     * Constructs a new PacketPlayer instance associated with the specified player.
     * This instance holds the player's details and retrieves the network channel
     * used for communication with the player.
     *
     * @param player the Player with whom this PacketPlayer instance is associated
     */
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

    /**
     * Sends multiple packets to the associated player by writing each packet
     * to the player's network channel and then flushing the channel.
     *
     * @param packet an array of {@link PacketWrapper} objects representing
     *               the packets to be sent. Each packet contains the data
     *               to be sent over the network, which is built using the
     *               {@code build()} method.
     */
    public void sendMultiplePackets(PacketWrapper... packet) {
        for (PacketWrapper p : packet) {
            channel.write(p.build());
        }
        channel.flush();
    }
}
