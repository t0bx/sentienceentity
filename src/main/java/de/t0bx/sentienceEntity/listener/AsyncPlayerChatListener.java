package de.t0bx.sentienceEntity.listener;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.npc.NpcsHandler;
import de.t0bx.sentienceEntity.npc.setup.NpcCreation;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static de.t0bx.sentienceEntity.utils.MessageUtils.sendMessage;

public class AsyncPlayerChatListener implements Listener {

    private final NpcCreation npcCreation;
    private final NpcsHandler npcsHandler;
    private final MiniMessage miniMessage;
    private final String prefix;

    public AsyncPlayerChatListener(NpcCreation npcCreation, NpcsHandler npcsHandler) {
        this.npcCreation = npcCreation;
        this.npcsHandler = npcsHandler;
        this.miniMessage = MiniMessage.miniMessage();
        this.prefix = SentienceEntity.getInstance().getPrefix();
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!this.npcCreation.isNpcCreation(player)) return;

        event.setCancelled(true);
        NpcCreation.NpcCreationBuilder builder = this.npcCreation.getCreationBuilder(player);

        switch (builder.getStep()) {
            case NAME -> {
                String name = miniMessage.serialize(event.message());
                builder.setName(name);
                sendMessage(player, miniMessage.deserialize(prefix + "Name set to: <white>" + name));

                Bukkit.getScheduler().runTask(SentienceEntity.getInstance(), () -> this.npcCreation.openInventory(player, 0));
                builder.nextStep();
            }

            case PLAYER_NAME -> {
                String name = miniMessage.serialize(event.message());
                builder.setPlayerName(name);
                builder.nextStep();

                sendMessage(player, miniMessage.deserialize(prefix + "Player name set to: <white>" + name));
                sendMessage(player, miniMessage.deserialize(prefix + "Should this npc only be visible with a certain permission? (Type <red>none <gray>for no permission)"));
            }

            case PERMISSION -> {
                String message = miniMessage.serialize(event.message());

                builder.setPermission(message);

                final String permission = message.contains("none") ? null : builder.getPermission();

                this.npcsHandler.createNPC(
                        builder.getName(),
                        builder.getEntityType(),
                        builder.getPlayerName(),
                        player.getLocation(),
                        permission
                );

                sendMessage(player, miniMessage.deserialize(prefix + "<green>You've created the npc " + builder.getName()));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.0f);
                this.npcCreation.removeCreationBuilder(player);
            }
        }
    }
}
