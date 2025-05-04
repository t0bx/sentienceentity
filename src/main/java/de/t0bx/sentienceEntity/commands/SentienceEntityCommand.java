package de.t0bx.sentienceEntity.commands;

import de.t0bx.sentienceEntity.SentienceEntity;
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

    public SentienceEntityCommand() {
        this.mm = MiniMessage.miniMessage();
        this.prefix = SentienceEntity.getInstance().getPrefix();
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
                if (args.length != 2) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se editnpc <Name>"));
                    return true;
                }

                this.handleEditNpc(player, args[1]);
            }

            case "removenpc" -> {
                if (args.length != 2) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se removenpc <Name>"));
                    return true;
                }

                this.handleRemoveNpc(player, args[1]);
            }

            default -> this.sendHelp(player);
        }
        return false;
    }

    private void handleSpawnNpc(Player player, @NotNull String npcName, @NotNull String skinName) {

    }

    private void handleEditNpc(Player player, @NotNull String npcName) {

    }

    private void handleRemoveNpc(Player player, @NotNull String npcName) {

    }

    private void sendHelp(Player player) {
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se spawnnpc <Name> <Skin>"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se editnpc <Name>"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se removenpc <Name>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
