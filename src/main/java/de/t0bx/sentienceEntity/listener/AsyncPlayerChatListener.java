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

    public AsyncPlayerChatListener(NpcCreation npcCreation, NpcsHandler npcsHandler) {
        this.npcCreation = npcCreation;
        this.npcsHandler = npcsHandler;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (this.npcCreation.isNpcCreation(player)) {
            event.setCancelled(true);
            NpcCreation.NpcCreationBuilder builder = this.npcCreation.getCreationBuilder(player);

            if (builder.getName() == null) {
                String name = MiniMessage.miniMessage().serialize(event.message());
                builder.setName(name);
                sendMessage(player, MiniMessage.miniMessage().deserialize(SentienceEntity.getInstance().getPrefix() + "Name set to: <white>" + name));

                Bukkit.getScheduler().runTask(SentienceEntity.getInstance(), () -> this.npcCreation.openInventory(player, 0));
                return;
            }

            if (builder.getPlayerName() == null) {
                String name = MiniMessage.miniMessage().serialize(event.message());
                builder.setPlayerName(name);

                sendMessage(player, MiniMessage.miniMessage().deserialize(SentienceEntity.getInstance().getPrefix() + "Player name set to: <white>" + name));
                this.npcsHandler.createNPC(
                        builder.getName(),
                        EntityType.PLAYER,
                        builder.getPlayerName(),
                        player.getLocation()
                );

                sendMessage(player, MiniMessage.miniMessage().deserialize(SentienceEntity.getInstance().getPrefix() + "<green>You've created the npc " + this.npcCreation.getCreationBuilder(player).getName()));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.0f);
                this.npcCreation.removeCreationBuilder(player);
            }
        }
    }
}
