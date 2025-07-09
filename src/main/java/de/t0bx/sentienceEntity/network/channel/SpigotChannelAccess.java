/**
 * SentienceEntity API License v1.1
 * Copyright (c) 2025 (t0bx)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to use, copy, modify, and integrate the Software into their own projects, including commercial and closed-source projects, subject to the following conditions:
 * <p>
 * 1. Attribution:
 * You must give appropriate credit to the original author ("Tobias Schuster" or "t0bx"), provide a link to the source or official page if available, and indicate if changes were made. You must do so in a reasonable and visible manner, such as in your plugin.yml, README, or about page.
 * <p>
 * 2. No Redistribution or Resale:
 * You may NOT sell, redistribute, or otherwise make the original Software or modified standalone versions of it available as a product (free or paid), plugin, or downloadable file, unless you have received prior written permission from the author. This includes publishing the plugin on any marketplace (e.g., SpigotMC, MC-Market, Polymart) or including it in paid bundles.
 * <p>
 * 3. Use as Dependency/API:
 * You are allowed to use this Software as a dependency or library in your own plugin or project, including in paid products, as long as attribution is given and the Software itself is not being sold or published separately.
 * <p>
 * 4. No Misrepresentation:
 * You may not misrepresent the origin of the Software. You must clearly distinguish your own modifications from the original work. The original author's name may not be removed from the source files or documentation.
 * <p>
 * 5. License Retention:
 * This license notice and all conditions must be preserved in all copies or substantial portions of the Software.
 * <p>
 * 6. Disclaimer:
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY ARISING FROM THE USE OF THIS SOFTWARE.
 * <p>
 * ---
 * <p>
 * Summary (non-binding):
 * You may use this plugin in your projects, even commercially, but you may not resell or republish it. Always give credit to t0bx.
 */

package de.t0bx.sentienceEntity.network.channel;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SpigotChannelAccess implements ChannelRegistry {

    private Method getHandleMethod;
    private Field connectionField;
    private Field networkManagerField;
    private Field channelField;
    private boolean initialized = false;

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
            ensureInitialized(player);

            Object handle = getHandleMethod.invoke(player);
            Object packetListener = connectionField.get(handle);
            Object networkManager = networkManagerField.get(packetListener);
            return (Channel) channelField.get(networkManager);
        } catch (Exception e) {
            throw new RuntimeException("Could not access Netty channel", e);
        }
    }

    /**
     * Ensures the initialization of reflective access to internal fields required for
     * retrieving the Netty channel associated with the given player. This method performs
     * reflection-based access to retrieve and store necessary class methods and fields
     * for later use in channel retrieval.
     *
     * @param player the Player instance for which initialization is performed
     * @throws Exception if initialization fails, including issues with reflection,
     *                   inaccessible fields/methods, or missing classes
     */
    private synchronized void ensureInitialized(Player player) throws Exception {
        if (initialized) return;

        this.getHandleMethod = player.getClass().getMethod("getHandle");
        this.getHandleMethod.setAccessible(true);

        Object handle = getHandleMethod.invoke(player);
        Class<?> entityPlayerClass = handle.getClass();

        this.connectionField = findFieldByClassName(
                entityPlayerClass,
                "net.minecraft.server.network.PlayerConnection"
        );

        Class<?> connectionClass = this.connectionField.getType();

        this.networkManagerField = getFieldFromSuper(
                connectionClass,
                Class.forName("net.minecraft.network.NetworkManager")
        );

        Class<?> networkManagerClass = this.networkManagerField.getType();

        this.channelField = getFieldFromSuper(
                networkManagerClass,
                Channel.class
        );

        initialized = true;
    }

    /**
     * Searches for a field of the specified type in the given class or its superclasses.
     * If a matching field is found, it is made accessible and returned.
     *
     * @param clazz the class to search for the field
     * @param fieldType the type of field to look for
     * @return the matching field from the class or its superclass
     * @throws NoSuchFieldException if no field of the specified type is found in the class hierarchy
     */
    private Field getFieldFromSuper(Class<?> clazz, Class<?> fieldType) throws NoSuchFieldException {
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (fieldType.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchFieldException("Field of type '" + fieldType.getName() + "' not found in class hierarchy.");
    }

    private Field findFieldByClassName(Class<?> clazz, String className) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().getName().equals(className)) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalStateException(
                "Field of class " + className + " not found in class " + clazz.getName());
    }
}
