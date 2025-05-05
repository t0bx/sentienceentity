package de.t0bx.sentienceEntity.commands;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.npc.NPCsHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SentienceEntityCommand implements CommandExecutor, TabCompleter {

    private final MiniMessage mm;
    private final String prefix;
    private final NPCsHandler npcsHandler;

    public SentienceEntityCommand() {
        this.mm = MiniMessage.miniMessage();
        this.prefix = SentienceEntity.getInstance().getPrefix();
        this.npcsHandler = SentienceEntity.getInstance().getNpcshandler();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        if (!player.hasPermission("se.command")) {
            player.sendMessage(this.mm.deserialize(this.prefix + "You don't have permission to execute this command!"));
            return true;
        }

        if (args.length == 0) {
            this.sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "spawnnpc" -> {
                if (args.length != 3) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se spawnnpc <Name> <Skin>"));
                    return true;
                }

                this.handleSpawnNpc(player, args[1], args[2]);
            }

            case "editnpc" -> {
                this.handleEditNpc(player, args);
            }

            case "removenpc" -> {
                if (args.length != 2) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se removenpc <Name> <Player Name>"));
                    return true;
                }

                this.handleRemoveNpc(player, args[1]);
            }

            default -> this.sendHelp(player);
        }
        return false;
    }

    private void handleSpawnNpc(Player player, @NotNull String npcName, @NotNull String skinName) {
        if (this.npcsHandler.doesNPCExist(npcName)) {
            player.sendMessage(this.mm.deserialize(this.prefix + "A npc with the name '" + npcName + "' already exists!"));
            return;
        }

        this.npcsHandler.createNPC(npcName, skinName, player.getLocation());
    }

    private void handleEditNpc(Player player, @NotNull String[] args) {
        if (args.length <= 2) {
            player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldLookAtPlayer"));
            player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldSneakWithPlayer"));
            player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se editnpc <Name> updateLocation"));
            player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se editnpc <Name> setSkin <Player Name>"));
            return;
        }

        String npcName = args[1];
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            player.sendMessage(this.mm.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        switch (args[2].toLowerCase()) {
            case "shouldlookatplayer" -> {
                if (args.length != 3) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldLookAtPlayer"));
                    return;
                }

                String response = this.npcsHandler.updateLookAtPlayer(npcName);
                switch (response.toLowerCase()) {
                    case "error" -> player.sendMessage(this.mm.deserialize(this.prefix + "There was an error updating the npc with the name '" + npcName + "'!"));
                    case "true" -> player.sendMessage(this.mm.deserialize(this.prefix + "The npc '" + npcName + "' will now look at the players!"));
                    case "false" -> player.sendMessage(this.mm.deserialize(this.prefix + "The npc '" + npcName + "' will no longer look at the players!"));
                }
            }

            case "shouldsneakwithplayer" -> {
                if (args.length != 3) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldLookAtPlayer"));
                    return;
                }

                String response = this.npcsHandler.updateSneakWithPlayer(npcName);
                switch (response.toLowerCase()) {
                    case "error" -> player.sendMessage(this.mm.deserialize(this.prefix + "There was an error updating the npc with the name '" + npcName + "'!"));
                    case "true" -> player.sendMessage(this.mm.deserialize(this.prefix + "The npc '" + npcName + "' will now sneak with players!"));
                    case "false" -> player.sendMessage(this.mm.deserialize(this.prefix + "The npc '" + npcName + "' will no longer sneak with players!"));
                }
            }

            case "updatelocation" -> {
                if (args.length != 3) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldLookAtPlayer"));
                    return;
                }

                this.npcsHandler.updateLocation(npcName, player.getLocation());
                player.sendMessage(this.mm.deserialize(this.prefix + "You've updated the Location for the npc " + npcName));
            }

            case "setskin" -> {
                if (args.length != 4) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldLookAtPlayer"));
                    return;
                }

                String playerName = args[3];
                this.npcsHandler.updateSkin(npcName, playerName);
                player.sendMessage(this.mm.deserialize(this.prefix + "You've updated the skin of the npc " + npcName));
            }
        }
    }

    private void handleRemoveNpc(Player player, @NotNull String npcName) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            player.sendMessage(this.mm.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        this.npcsHandler.removeNPC(npcName);
    }

    private void sendHelp(Player player) {
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se spawnnpc <Name> <Player Name>"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se editnpc <Name>"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se removenpc <Name>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
