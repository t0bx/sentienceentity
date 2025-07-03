package de.t0bx.sentienceEntity.network.channel;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class PaperChannelAccess implements ChannelRegistry {

    /**
     * Retrieves the Netty channel associated with the specified player by accessing internal fields
     * through reflection. This method attempts to locate and return the player's channel object.
     *
     * @param player the Player instance whose Netty channel is to be retrieved
     * @return the Netty channel associated with the specified player
     * @throws RuntimeException if the Netty channel cannot be accessed
     */
    public Channel getChannel(Player player) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);

            Field connField = handle.getClass().getDeclaredField("connection");
            connField.setAccessible(true);
            Object packetListener = connField.get(handle);

            Field networkManagerField = getFieldFromSuper(packetListener.getClass(), "connection");
            networkManagerField.setAccessible(true);
            Object networkManager = networkManagerField.get(packetListener);

            Field channelField = getFieldFromSuper(networkManager.getClass(), "channel");
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
