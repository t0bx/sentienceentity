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

    /**
     * Retrieves the player associated with the specified network channel.
     *
     * @param channel the network channel for which the associated player is to be retrieved
     * @return the player associated with the given channel, or null if no player is found
     */
    public Player getPlayer(Channel channel) {
        return mappedChannels.getOrDefault(channel, null);
    }

    /**
     * Removes the specified player from the internal packet handling system by
     * unregistering their associated {@link PacketPlayer} and its corresponding
     * network channel.
     *
     * @param player the {@link Player} to be removed from the packet handling system
     */
    public void removePlayer(Player player) {
        PacketPlayer packetPlayer = this.cachedChannels.remove(player);
        if (packetPlayer != null) {
            this.mappedChannels.remove(packetPlayer.getChannel());
        }
    }
}
