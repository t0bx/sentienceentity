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

package de.t0bx.sentienceEntity.network.channel;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class SpigotChannelAccess implements ChannelRegistry {

    /**
     * Retrieves the Netty channel associated with the provided player by accessing internal fields
     * through reflection. This method attempts to locate and return the player's channel object.
     *
     * @param player the Player instance whose Netty channel is to be retrieved
     * @return the Netty channel associated with the provided player
     * @throws RuntimeException if the Netty channel cannot be accessed
     */
    public Channel getChannel(Player player) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);

            Field connField = handle.getClass().getDeclaredField("f");
            connField.setAccessible(true);
            Object packetListener = connField.get(handle);

            Field networkManagerField = getFieldFromSuper(packetListener.getClass(), "e");
            networkManagerField.setAccessible(true);
            Object networkManager = networkManagerField.get(packetListener);

            Field channelField = getFieldFromSuper(networkManager.getClass(), "n");
            channelField.setAccessible(true);
            return (Channel) channelField.get(networkManager);

        } catch (Exception e) {
            throw new RuntimeException("Could not access Netty channel", e);
        }
    }

    /**
     * Searches for a declared field with the specified name within the specified class and its superclasses.
     *
     * @param clazz the class from which the search starts
     * @param fieldName the name of the field to find
     * @return the Field object representing the field with the specified name
     * @throws NoSuchFieldException if the field cannot be found in the class hierarchy
     */
    private Field getFieldFromSuper(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field '" + fieldName + "' not found in class hierarchy.");
    }
}
