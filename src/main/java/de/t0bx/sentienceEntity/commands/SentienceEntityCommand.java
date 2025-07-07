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

package de.t0bx.sentienceEntity.commands;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.hologram.HologramLine;
import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.npc.NpcsHandler;
import de.t0bx.sentienceEntity.npc.SentienceNPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SentienceEntityCommand implements CommandExecutor, TabCompleter {

    private final MiniMessage miniMessage;
    private final String prefix;
    private final NpcsHandler npcsHandler;
    private final HologramManager hologramManager;

    public SentienceEntityCommand(SentienceEntity sentienceEntity) {
        this.miniMessage = MiniMessage.miniMessage();
        this.prefix = sentienceEntity.getPrefix();
        this.npcsHandler = sentienceEntity.getNpcshandler();
        this.hologramManager = sentienceEntity.getHologramManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        if (!player.hasPermission("se.command")) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "You don't have permission to execute this command!"));
            return true;
        }

        if (SentienceEntity.getApi().isApiOnly()) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "This plugin works just as api-only!"));
            return true;
        }

        if (args.length == 0) {
            this.sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "spawnnpc" -> {
                if (args.length != 3) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se spawnnpc <Name> <Skin>"));
                    return true;
                }

                this.handleSpawnNpc(player, args[1], args[2]);
            }

            case "editnpc" -> {
                this.handleEditNpc(player, args);
            }

            case "removenpc" -> {
                if (args.length != 2) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se removenpc <Name> <Player Name>"));
                    return true;
                }

                this.handleRemoveNpc(player, args[1]);
            }

            case "listnpc" -> {
                if (args.length != 1) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se listnpc <dark_gray>| <gray>List all npcs"));
                    return true;
                }

                this.handleListNpcs(player);
            }

            case "createhologram" -> {
                if (args.length != 2) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se createHologram <Name> <dark_gray>| <gray>Create a Hologram for a npc"));
                    return true;
                }

                String npcName = args[1];
                this.handleCreateHologram(player, npcName);
            }

            case "addline" -> {
                if (args.length <= 2) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se addLine <Name> <Text> <dark_gray>| <gray>Add a line for a hologram"));
                    return true;
                }

                String npcName = args[1];
                String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                this.handleAddLine(player, npcName, text);
            }

            case "setline" -> {
                if (args.length <= 3) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se setLine <Name> <index> <Text> <dark_gray>| <gray>Updates a specific line from a hologram"));
                    return true;
                }

                String npcName = args[1];
                int index = Integer.parseInt(args[2]);
                String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                this.handleSetLine(player, npcName, index, text);
            }

            case "lines" -> {
                if (args.length != 2) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se lines <Name> <dark_gray>| <gray>List all lines from a hologram"));
                    return true;
                }

                String npcName = args[1];
                this.handleHologramLines(player, npcName);
            }

            case "removeline" -> {
                if (args.length != 3) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se removeLine <Name> <index> <dark_gray>| <gray>Removes a specific line from a hologram"));
                    return true;
                }

                String npcName = args[1];
                int index = Integer.parseInt(args[2]);
                this.handleRemoveLine(player, npcName, index);
            }

            case "removehologram" -> {
                if (args.length != 2) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se removeHologram <Name> <dark_gray>| <gray>Removes a hologram"));
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
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "A npc with the name '" + npcName + "' already exists!"));
            return;
        }

        this.npcsHandler.createNPC(npcName, skinName, player.getLocation());
    }

    private void handleEditNpc(Player player, @NotNull String[] args) {
        if (args.length <= 2) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldLookAtPlayer"));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldSneakWithPlayer"));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> updateLocation"));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> setSkin <Player Name>"));
            return;
        }

        String npcName = args[1];
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        switch (args[2].toLowerCase()) {
            case "shouldlookatplayer" -> {
                if (args.length != 3) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldLookAtPlayer"));
                    return;
                }

                String response = this.npcsHandler.updateLookAtPlayer(npcName);
                switch (response.toLowerCase()) {
                    case "error" -> sendMessage(player, this.miniMessage.deserialize(this.prefix + "There was an error updating the npc with the name '" + npcName + "'!"));
                    case "true" -> sendMessage(player, this.miniMessage.deserialize(this.prefix + "The npc '" + npcName + "' will now look at players!"));
                    case "false" -> sendMessage(player, this.miniMessage.deserialize(this.prefix + "The npc '" + npcName + "' will no longer look at players!"));
                }
            }

            case "shouldsneakwithplayer" -> {
                if (args.length != 3) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldLookAtPlayer"));
                    return;
                }

                String response = this.npcsHandler.updateSneakWithPlayer(npcName);
                switch (response.toLowerCase()) {
                    case "error" -> sendMessage(player, this.miniMessage.deserialize(this.prefix + "There was an error updating the npc with the name '" + npcName + "'!"));
                    case "true" -> sendMessage(player, this.miniMessage.deserialize(this.prefix + "The npc '" + npcName + "' will now sneak with players!"));
                    case "false" -> sendMessage(player, this.miniMessage.deserialize(this.prefix + "The npc '" + npcName + "' will no longer sneak with players!"));
                }
            }

            case "updatelocation" -> {
                if (args.length != 3) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldLookAtPlayer"));
                    return;
                }

                this.npcsHandler.updateLocation(npcName, player.getLocation());
                sendMessage(player, this.miniMessage.deserialize(this.prefix + "You've updated the Location for the npc '" + npcName + "'"));
            }

            case "setskin" -> {
                if (args.length != 4) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldLookAtPlayer"));
                    return;
                }

                String playerName = args[3];
                this.npcsHandler.updateSkin(npcName, playerName, true);
                sendMessage(player, this.miniMessage.deserialize(this.prefix + "You've updated the skin of the npc '" + npcName + "'"));
            }
        }
    }

    private void handleRemoveNpc(Player player, @NotNull String npcName) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        this.npcsHandler.removeNPC(npcName);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "You have removed the npc '" + npcName + "'."));
    }

    private void handleListNpcs(Player player) {
        if (this.npcsHandler.getNPCMap().isEmpty()) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "You haven't spawned any NPCs yet."));
            return;
        }

        this.npcsHandler.getNPCMap().forEach((npcName, npc) -> {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + npcName + " | X: " + npc.getLocation().getX() + " | Y: " + npc.getLocation().getY() + " | Z: " + npc.getLocation().getZ()));
        });
    }

    private void handleCreateHologram(Player player, String npcName) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        SentienceNPC npc = this.npcsHandler.getNPC(npcName);
        this.hologramManager.createHologram(npcName, npc.getLocation());
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "You have created a hologram for the npc '" + npcName + "'."));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Use /se addLine to add a line to the hologram!"));
    }

    private void handleAddLine(Player player, String npcName, String text) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        this.hologramManager.show(player, npcName);
        this.hologramManager.addLine(npcName, text, true);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "You have added a line to the hologram for the npc '" + npcName + "'."));
    }

    private void handleSetLine(Player player, String npcName, int index, String text) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        if (!this.hologramManager.doesLineExist(npcName, index)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no line with the index '" + index + "'!"));
            return;
        }

        this.hologramManager.updateLine(npcName, index, text);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "You've changed the line '" + index + "' to '" + text + "'!"));

    }

    private void handleRemoveLine(Player player, String npcName, int index) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        if (!this.hologramManager.doesLineExist(npcName, index)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no line with the index '" + index + "'!"));
            return;
        }

        this.hologramManager.removeLine(npcName, index);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "You've removed the line with the index '" + index + "'"));
    }

    private void handleHologramLines(Player player, String npcName) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        Map<Integer, HologramLine> hologramLines = this.hologramManager.getHologramLines(npcName);
        if (hologramLines.isEmpty()) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There are no HologramLines for the npc '" + npcName + "'!"));
            return;
        }

        hologramLines.forEach((index, hologramline) -> {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Index: '" + index + "', Text: '" + hologramline.getText() + "'"));
        });
    }

    private void handleRemoveHologram(Player player, String npcName) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        Map<Integer, HologramLine> hologramLines = this.hologramManager.getHologramLines(npcName);
        if (hologramLines.isEmpty()) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There are no HologramLines for the npc '" + npcName + "'!"));
            return;
        }

        this.hologramManager.removeHologram(npcName);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "You've removed the hologram for the npc '" + npcName + "'!"));
    }

    private void sendHelp(Player player) {
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "NPC-Management: "));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se spawnnpc <Name> <Player Name> <dark_gray>| <gray>Spawn a new npc"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> <dark_gray>| <gray>Edit a npc"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se removenpc <Name> <dark_gray>| <gray>Removes a npc"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se listnpc <dark_gray>| <gray>List all npcs"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "NPC-Bound Holograms: "));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se createHologram <Name> <dark_gray>| <gray>Create a Hologram for a npc"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se addLine <Name> <Text> <dark_gray>| <gray>Add a line for a hologram"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se setLine <Name> <index> <Text> <dark_gray>| <gray>Updates a specific line from a hologram"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se lines <Name> <dark_gray>| <gray>List all lines from a hologram"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se removeLine <Name> <index> <dark_gray>| <gray>Removes a specific line from a hologram"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se removeHologram <Name> <dark_gray>| <gray>Removes a hologram"));
    }
    
    private void sendMessage(Player player, Component component) {
        if (SentienceEntity.getInstance().isPAPER()) {
            player.sendMessage(component);
        } else {
            String legacy = LegacyComponentSerializer.legacySection().serialize(component);
            player.sendMessage(legacy);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return List.of("spawnnpc", "editnpc", "listnpc", "createhologram", "addline", "setline", "lines", "removeLine", "removeHologram");
        }

        List<String> npcNames = this.npcsHandler.getNPCNames();
        switch (args[0].toLowerCase()) {
            case "spawnnpc" -> {
                if (args.length == 2) {
                    return Collections.singletonList("<Name>");
                }

                if (args.length == 3) {
                    return Collections.singletonList("<Player Name>");
                }
            }

            case "removenpc", "createhologram", "removehologram", "lines" -> {
                return npcNames;
            }

            case "editnpc" -> {
                if (args.length == 2) {
                    return npcNames;
                } else if (args.length == 3) {
                    return List.of("shouldLookAtPlayer", "shouldSneakWithPlayer", "updateLocation", "setSkin");
                } else if (args.length == 4) {
                    if (args[2].equalsIgnoreCase("setSkin")) {
                        return Collections.singletonList("<Player Name>");
                    } else {
                        return Collections.emptyList();
                    }
                }
            }

            case "addline" -> {
                if (args.length == 2) {
                    return npcNames;
                }

                if (args.length == 3) {
                    return Collections.singletonList("<Text>");
                }
            }

            case "setline" -> {
                if (args.length == 2) {
                    return npcNames;
                }

                if (args.length == 3) {
                    if (this.hologramManager.getHologramLines(args[1]) == null) return Collections.emptyList();

                    return this.hologramManager.getHologramLines(args[1]).keySet().stream().map(String::valueOf).collect(Collectors.toList());
                }

                if (args.length == 4) {
                    return Collections.singletonList("<Text>");
                }
            }

            case "removeline" -> {
                if (args.length == 2) {
                    return npcNames;
                }

                if (args.length == 3) {
                    if (this.hologramManager.getHologramLines(args[1]) == null) return Collections.emptyList();

                    return this.hologramManager.getHologramLines(args[1]).keySet().stream().map(String::valueOf).collect(Collectors.toList());
                }
            }
        }

        return Collections.emptyList();
    }
}