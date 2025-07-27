package de.t0bx.sentienceEntity.utils;

import de.t0bx.sentienceEntity.SentienceEntity;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class MessageUtils {

    public static void sendMessage(Player player, Component component) {
        if (SentienceEntity.getInstance().isPAPER()) {
            player.sendMessage(component);
        } else {
            SentienceEntity.getInstance().getAudiences().player(player).sendMessage(component);
        }
    }
}
