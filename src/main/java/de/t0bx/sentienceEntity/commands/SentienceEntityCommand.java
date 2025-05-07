package de.t0bx.sentienceEntity.commands;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.hologram.HologramLine;
import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.npc.NPCsHandler;
import de.t0bx.sentienceEntity.npc.SentienceNPC;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SentienceEntityCommand implements CommandExecutor, TabCompleter {

    private final MiniMessage mm;
    private final String prefix;
    private final NPCsHandler npcsHandler;
    private final HologramManager hologramManager;

    public SentienceEntityCommand() {
        this.mm = MiniMessage.miniMessage();
        this.prefix = SentienceEntity.getInstance().getPrefix();
        this.npcsHandler = SentienceEntity.getInstance().getNpcshandler();
        this.hologramManager = SentienceEntity.getInstance().getHologramManager();
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

            case "createhologram" -> {
                if (args.length != 2) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se createHologram <Name> <dark_gray>| <gray>Create a Hologram for a npc"));
                    return true;
                }

                String npcName = args[1];
                this.handleCreateHologram(player, npcName);
            }

            case "addline" -> {
                if (args.length <= 2) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se addLine <Name> <Text> <dark_gray>| <gray>Add a line for a hologram"));
                    return true;
                }

                String npcName = args[1];
                String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                this.handleAddLine(player, npcName, text);
            }

            case "setline" -> {
                if (args.length <= 3) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se setLine <Name> <index> <Text> <dark_gray>| <gray>Updates a specific line from a hologram"));
                    return true;
                }

                String npcName = args[1];
                int index = Integer.parseInt(args[2]);
                String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                this.handleSetLine(player, npcName, index, text);
            }

            case "lines" -> {
                if (args.length != 2) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se lines <Name> <dark_gray>| <gray>List all lines from a hologram"));
                    return true;
                }

                String npcName = args[1];
                this.handleHologramLines(player, npcName);
            }

            case "removeline" -> {
                if (args.length != 3) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se removeLine <Name> <index> <dark_gray>| <gray>Removes a specific line from a hologram"));
                    return true;
                }

                String npcName = args[1];
                int index = Integer.parseInt(args[2]);
                this.handleRemoveLine(player, npcName, index);
            }

            case "removehologram" -> {
                if (args.length != 2) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se removeHologram <Name> <dark_gray>| <gray>Removes a hologram"));
                    return true;
                }

                String npcName = args[1];
                this.handleRemoveHologram(player, npcName);
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
                    case "true" -> player.sendMessage(this.mm.deserialize(this.prefix + "The npc '" + npcName + "' will now look at players!"));
                    case "false" -> player.sendMessage(this.mm.deserialize(this.prefix + "The npc '" + npcName + "' will no longer look at players!"));
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
                player.sendMessage(this.mm.deserialize(this.prefix + "You've updated the Location for the npc '" + npcName + "'"));
            }

            case "setskin" -> {
                if (args.length != 4) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldLookAtPlayer"));
                    return;
                }

                String playerName = args[3];
                this.npcsHandler.updateSkin(npcName, playerName);
                player.sendMessage(this.mm.deserialize(this.prefix + "You've updated the skin of the npc '" + npcName + "'"));
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

    private void handleCreateHologram(Player player, String npcName) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            player.sendMessage(this.mm.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        SentienceNPC npc = this.npcsHandler.getNPC(npcName);
        this.hologramManager.createHologram(npcName, npc.getLocation());
        player.sendMessage(this.mm.deserialize(this.prefix + "You have created a hologram for the npc '" + npcName + "'."));
        player.sendMessage(this.mm.deserialize(this.prefix + "Use /se addLine to add a line to the hologram!"));
    }

    private void handleAddLine(Player player, String npcName, String text) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            player.sendMessage(this.mm.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        this.hologramManager.show(player, npcName);
        this.hologramManager.addLine(npcName, text);
        player.sendMessage(this.mm.deserialize(this.prefix + "You have added a line to the hologram for the npc '" + npcName + "'."));
    }

    private void handleSetLine(Player player, String npcName, int index, String text) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            player.sendMessage(this.mm.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        if (!this.hologramManager.doesLineExist(npcName, index)) {
            player.sendMessage(this.mm.deserialize(this.prefix + "There is no line with the index '" + index + "'!"));
            return;
        }

        this.hologramManager.updateLine(npcName, index, text);
        player.sendMessage(this.mm.deserialize(this.prefix + "You've changed the line '" + index + "' to '" + text + "'!"));

    }

    private void handleRemoveLine(Player player, String npcName, int index) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            player.sendMessage(this.mm.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        if (!this.hologramManager.doesLineExist(npcName, index)) {
            player.sendMessage(this.mm.deserialize(this.prefix + "There is no line with the index '" + index + "'!"));
            return;
        }

        this.hologramManager.removeLine(npcName, index);
        player.sendMessage(this.mm.deserialize(this.prefix + "You've removed the line with the index '" + index + "'"));
    }

    private void handleHologramLines(Player player, String npcName) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            player.sendMessage(this.mm.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        Map<Integer, HologramLine> hologramLines = this.hologramManager.getHologramLines(npcName);
        if (hologramLines.isEmpty()) {
            player.sendMessage(this.mm.deserialize(this.prefix + "There are no HologramLines for the npc '" + npcName + "'!"));
            return;
        }

        hologramLines.forEach((index, hologramline) -> {
            player.sendMessage(this.mm.deserialize(this.prefix + "Index: '" + index + "', Text: '" + hologramline.getText() + "'"));
        });
    }

    private void handleRemoveHologram(Player player, String npcName) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            player.sendMessage(this.mm.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        Map<Integer, HologramLine> hologramLines = this.hologramManager.getHologramLines(npcName);
        if (hologramLines.isEmpty()) {
            player.sendMessage(this.mm.deserialize(this.prefix + "There are no HologramLines for the npc '" + npcName + "'!"));
            return;
        }

        this.hologramManager.removeHologram(npcName);
        player.sendMessage(this.mm.deserialize(this.prefix + "You've removed the hologram for the npc '" + npcName + "'!"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(this.mm.deserialize(this.prefix + "NPC-Management: "));
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se spawnnpc <Name> <Player Name> <dark_gray>| <gray>Spawn a new npc"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se editnpc <Name> <dark_gray>| <gray>Edit a npc"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se removenpc <Name> <dark_gray>| <gray>Removes a npc"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se listnpc <dark_gray>| <gray>List all npcs"));
        player.sendMessage(this.mm.deserialize(this.prefix + "NPC-Bound Holograms: "));
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se createHologram <Name> <dark_gray>| <gray>Create a Hologram for a npc"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se addLine <Name> <Text> <dark_gray>| <gray>Add a line for a hologram"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se setLine <Name> <index> <Text> <dark_gray>| <gray>Updates a specific line from a hologram"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se lines <Name> <dark_gray>| <gray>List all lines from a hologram"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se removeLine <Name> <index> <dark_gray>| <gray>Removes a specific line from a hologram"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Usage: /se removeHologram <Name> <dark_gray>| <gray>Removes a hologram"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        return List.of();
    }
}