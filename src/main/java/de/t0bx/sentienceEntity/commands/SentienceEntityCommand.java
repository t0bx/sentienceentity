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
import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.network.inventory.equipment.EquipmentSlot;
import de.t0bx.sentienceEntity.npc.NpcsHandler;
import de.t0bx.sentienceEntity.npc.setup.NpcCreation;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static de.t0bx.sentienceEntity.utils.MessageUtils.sendMessage;

public class SentienceEntityCommand implements CommandExecutor, TabCompleter {

    private final MiniMessage miniMessage;
    private final String prefix;
    private final NpcCreation npcCreation;
    private final NpcsHandler npcsHandler;

    private final List<Player> inspectList;

    private static final Set<String> VALID_TYPES = Set.of("mainhand", "offhand", "boots", "leggings", "chestplate", "helmet");
    private static final Map<String, String> REQUIRED_SUFFIX = Map.of(
            "boots", "BOOTS",
            "leggings", "LEGGINGS",
            "chestplate", "CHESTPLATE",
            "helmet", "HELMET"
    );

    public SentienceEntityCommand(SentienceEntity sentienceEntity) {
        this.miniMessage = MiniMessage.miniMessage();
        this.prefix = sentienceEntity.getPrefix();
        this.npcCreation = sentienceEntity.getNpcCreation();
        this.npcsHandler = sentienceEntity.getNpcshandler();
        this.inspectList = sentienceEntity.getInspectList();
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
                if (args.length != 1) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se spawnnpc"));
                    return true;
                }

                this.handleSpawnNpc(player);
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

            case "inspect" -> {
                if (args.length != 1) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se inspect <dark_gray>| <gray>Enter Inspector Mode"));
                    return true;
                }

                this.handleInspect(player);
            }

            default -> this.sendHelp(player);
        }
        return false;
    }

    private void handleSpawnNpc(Player player) {
        if (this.npcCreation.isNpcCreation(player)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "You can't spawn npcs while you are creating one!"));
            return;
        }

        this.npcCreation.addCreationBuilder(player);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Please type in the name of the npc you want to create!"));
    }

    private void handleEditNpc(Player player, @NotNull String[] args) {
        if (args.length <= 2) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldLookAtPlayer"));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldSneakWithPlayer"));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> updateLocation"));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> setSkin <Player Name>"));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> setItem <Mainhand, Offhand, Boots, Leggings, Chestplate, Helmet>"));
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

            case "setitem" -> {
                if (args.length != 4) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> setItem <Mainhand, Offhand, Boots, Leggings, Chestplate, Helmet>"));
                    return;
                }

                ItemStack itemStack = player.getInventory().getItemInMainHand();
                if (itemStack.getType() == Material.AIR) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "You need to hold an item in your hand!"));
                    return;
                }

                String type = args[3].toLowerCase();
                if (!VALID_TYPES.contains(type)) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "The type '" + type + "' is not valid!"));
                    return;
                }

                if (REQUIRED_SUFFIX.containsKey(type)) {
                    String requiredSuffix = REQUIRED_SUFFIX.get(type);
                    if (!itemStack.getType().name().endsWith(requiredSuffix)) {
                        sendMessage(player, this.miniMessage.deserialize(this.prefix + "The item in your hand needs to be a " + type + "!"));
                        return;
                    }
                }

                EquipmentSlot equipmentSlot;
                switch (type) {
                    case "mainhand" -> equipmentSlot = EquipmentSlot.MAIN_HAND;
                    case "offhand" -> equipmentSlot = EquipmentSlot.OFF_HAND;
                    case "boots" -> equipmentSlot = EquipmentSlot.BOOTS;
                    case "leggings" -> equipmentSlot = EquipmentSlot.LEGGINGS;
                    case "chestplate" -> equipmentSlot = EquipmentSlot.CHEST_PLATE;
                    case "helmet" -> equipmentSlot = EquipmentSlot.HELMET;
                    default -> equipmentSlot = null;
                }
                if (equipmentSlot == null) return;

                this.npcsHandler.updateEquipment(npcName, equipmentSlot, itemStack);
            }

            case "removeitem" -> {
                if (args.length != 4) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> removeItem <Mainhand, Offhand, Boots, Leggings, Chestplate, Helmet>"));
                    return;
                }

                String type = args[3].toLowerCase();
                if (!VALID_TYPES.contains(type)) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "The type '" + type + "' is not valid!"));
                    return;
                }

                EquipmentSlot equipmentSlot;
                switch (type) {
                    case "mainhand" -> equipmentSlot = EquipmentSlot.MAIN_HAND;
                    case "offhand" -> equipmentSlot = EquipmentSlot.OFF_HAND;
                    case "boots" -> equipmentSlot = EquipmentSlot.BOOTS;
                    case "leggings" -> equipmentSlot = EquipmentSlot.LEGGINGS;
                    case "chestplate" -> equipmentSlot = EquipmentSlot.CHEST_PLATE;
                    case "helmet" -> equipmentSlot = EquipmentSlot.HELMET;
                    default -> equipmentSlot = null;
                }
                if (equipmentSlot == null) return;

                this.npcsHandler.updateEquipment(npcName, equipmentSlot, null);
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

    private void handleInspect(Player player) {
        if (this.inspectList.contains(player)) {
            this.inspectList.remove(player);
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "<red>Inspector Mode disabled."));
            return;
        }

        this.inspectList.add(player);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "<green>Inspector Mode enabled."));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Left-Click on a Npc to get information about the npc."));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Right-Click on a Npc to get information about the hologram."));
    }

    private void sendHelp(Player player) {
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "NPC-Management: "));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se spawnnpc <Name> <Player Name> <dark_gray>| <gray>Spawn a new npc"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> <dark_gray>| <gray>Edit a npc"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se removenpc <Name> <dark_gray>| <gray>Removes a npc"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se listnpc <dark_gray>| <gray>List all npcs"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se inspect <dark_gray>| <gray>Enter Inspector Mode"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return List.of(
                    "spawnnpc", "editnpc", "listnpc", "removenpc", "inspect"
            );
        }

        List<String> npcNames = this.npcsHandler.getNPCNames();
        switch (args[0].toLowerCase()) {
            case "removenpc" -> {
                return npcNames;
            }

            case "editnpc" -> {
                if (args.length == 2) {
                    return npcNames;
                } else if (args.length == 3) {
                    return List.of("shouldLookAtPlayer", "shouldSneakWithPlayer", "updateLocation", "setSkin", "setItem", "removeItem");
                } else if (args.length == 4) {
                    return switch (args[2].toLowerCase()) {
                        case "setskin" -> Collections.singletonList("<Player Name>");
                        case "setitem", "removeitem" -> List.of("Mainhand", "Offhand", "Boots", "Leggings", "Chestplate", "Helmet");
                        default -> Collections.emptyList();
                    };
                }
            }
        }

        return Collections.emptyList();
    }
}