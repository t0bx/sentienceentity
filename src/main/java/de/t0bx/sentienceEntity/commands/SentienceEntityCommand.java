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
import de.t0bx.sentienceEntity.network.inventory.equipment.Equipment;
import de.t0bx.sentienceEntity.network.inventory.equipment.EquipmentSlot;
import de.t0bx.sentienceEntity.npc.NpcsHandler;
import de.t0bx.sentienceEntity.npc.SentienceNPC;
import de.t0bx.sentienceEntity.npc.setup.NpcCreation;
import de.t0bx.sentienceEntity.path.SentiencePathHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
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
    private final SentiencePathHandler pathHandler;

    private final List<Player> inspectList;

    private static final Set<String> VALID_TYPES = Set.of("mainhand", "offhand", "boots", "leggings", "chestplate", "helmet");
    private static final Map<String, String> REQUIRED_SUFFIX = Map.of(
            "boots", "BOOTS",
            "leggings", "LEGGINGS",
            "chestplate", "CHESTPLATE",
            "helmet", "HELMET"
    );

    private static final Set<EntityType> VALID_ENTITY_TYPES = Set.of(
            EntityType.PLAYER,
            EntityType.DROWNED,
            EntityType.PIGLIN_BRUTE,
            EntityType.PIGLIN,
            EntityType.SKELETON,
            EntityType.WITHER_SKELETON,
            EntityType.ZOMBIE,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.ZOMBIFIED_PIGLIN
    );

    private static final Map<String, EquipmentSlot> TYPE_TO_SLOT = Map.of(
            "mainhand", EquipmentSlot.MAIN_HAND,
            "offhand", EquipmentSlot.OFF_HAND,
            "boots", EquipmentSlot.BOOTS,
            "leggings", EquipmentSlot.LEGGINGS,
            "chestplate", EquipmentSlot.CHEST_PLATE,
            "helmet", EquipmentSlot.HELMET
    );

    public SentienceEntityCommand(SentienceEntity sentienceEntity) {
        this.miniMessage = MiniMessage.miniMessage();
        this.prefix = sentienceEntity.getPrefix();
        this.npcCreation = sentienceEntity.getNpcCreation();
        this.npcsHandler = sentienceEntity.getNpcshandler();
        this.inspectList = sentienceEntity.getInspectList();
        this.pathHandler = sentienceEntity.getSentiencePathHandler();
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
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> removeItem <Mainhand, Offhand, Boots, Leggings, Chestplate, Helmet>"));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> setPermission <Permission> <dark_gray>| <gray>Set a permission for the npc <red>none <gray>for no permission"));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> setPath <Path Name> <dark_gray>| <gray>Bound a path to the npc."));
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
                    case "error" -> sendMessage(player, this.miniMessage.deserialize(this.prefix + "<red>There was an error updating the npc with the name '" + npcName + "'!"));
                    case "true" -> sendMessage(player, this.miniMessage.deserialize(this.prefix + "The npc '" + npcName + "' will now look at players!"));
                    case "false" -> sendMessage(player, this.miniMessage.deserialize(this.prefix + "The npc '" + npcName + "' will no longer look at players!"));
                }
            }

            case "shouldsneakwithplayer" -> {
                if (args.length != 3) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> shouldLookAtPlayer"));
                    return;
                }

                SentienceNPC npc = this.npcsHandler.getNPC(npcName);
                if (npc.getEntityType() != EntityType.PLAYER) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "<red>The npc '" + npcName + "' is not a player!"));
                    return;
                }

                String response = this.npcsHandler.updateSneakWithPlayer(npcName);
                switch (response.toLowerCase()) {
                    case "error" -> sendMessage(player, this.miniMessage.deserialize(this.prefix + "<red>There was an error updating the npc with the name '" + npcName + "'!"));
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

                SentienceNPC npc = this.npcsHandler.getNPC(npcName);
                if (npc.getEntityType() != EntityType.PLAYER) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "<red>The npc '" + npcName + "' is not a player!"));
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
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "<red>You need to hold an item in your hand!"));
                    return;
                }

                String type = args[3].toLowerCase();
                if (!VALID_TYPES.contains(type)) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "<red>The type '" + type + "' is not valid!"));
                    return;
                }

                SentienceNPC npc = this.npcsHandler.getNPC(npcName);

                if (npc.getEntityType() == EntityType.PILLAGER && !(type.equalsIgnoreCase("mainhand") || type.equalsIgnoreCase("offhand"))) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "<red>The pillager can't have an item in the '" + type + "' slot!"));
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "<red>You can only set an item in the mainhand or offhand!"));
                    return;
                } else if (!VALID_ENTITY_TYPES.contains(npc.getEntityType()) && npc.getEntityType() != EntityType.PILLAGER) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "<red>The npc '" + npcName + "' can't have an item in the '" + type + "' slot!"));
                    return;
                }

                if (REQUIRED_SUFFIX.containsKey(type)) {
                    String requiredSuffix = REQUIRED_SUFFIX.get(type);
                    if (!itemStack.getType().name().endsWith(requiredSuffix)) {
                        sendMessage(player, this.miniMessage.deserialize(this.prefix + "<red>The item in your hand needs to be a " + type + "!"));
                        return;
                    }
                }

                EquipmentSlot equipmentSlot = TYPE_TO_SLOT.get(type);
                if (equipmentSlot == null) return;

                SentienceNPC.EquipmentData equipmentData = npc.getEquipmentData();
                if (equipmentData != null) {
                    for (Equipment equipment : equipmentData.getEquipment() ) {
                        if (equipment.getSlot() == equipmentSlot &&
                                equipment.getItemStack().getType() == itemStack.getType() ) {
                            sendMessage(player, this.miniMessage.deserialize(this.prefix + "<red>The item in your hand is already equipped in the '" + type + "' slot!"));
                            return;
                        }
                    }
                }

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

                EquipmentSlot equipmentSlot = TYPE_TO_SLOT.get(type);
                if (equipmentSlot == null) return;

                this.npcsHandler.updateEquipment(npcName, equipmentSlot, null);
            }

            case "setpermission" -> {
                if (args.length != 4) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> setPermission <Permission> <dark_gray>| <gray>Set a permission for the npc <red>none <gray>for no permission"));
                    return;
                }

                String permission = args[3].equalsIgnoreCase("none") ? null : args[3];
                this.npcsHandler.updatePermission(npcName, permission);

                String message = permission == null ?
                        "The npc '" + npcName + "' will now be visible with no permission!" :
                        "The npc '" + npcName + "' will now be only visible with the permission '" + permission + "'!";

                sendMessage(player, this.miniMessage.deserialize(this.prefix + message));
            }

            case "setpath" -> {
                if (args.length != 4) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /se editnpc <Name> setPath <Path Name> <dark_gray>| <gray>Bound a path to the npc."));
                    return;
                }

                String pathName = args[3];
                if (!pathHandler.doesPathNameExist(pathName)) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "<red>There is no path with the name '" + pathName + "'!"));
                    return;
                }

                this.npcsHandler.setPath(npcName, pathName);
                sendMessage(player, this.miniMessage.deserialize(this.prefix + "You've updated the path of the npc '" + npcName + "'"));
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
                    return List.of("shouldLookAtPlayer", "shouldSneakWithPlayer", "updateLocation", "setSkin", "setItem", "removeItem", "setPermission", "setPath");
                } else if (args.length == 4) {
                    return switch (args[2].toLowerCase()) {
                        case "setskin" -> Collections.singletonList("<Player Name>");
                        case "setitem", "removeitem" -> List.of("Mainhand", "Offhand", "Boots", "Leggings", "Chestplate", "Helmet");
                        case "setpermission" -> List.of("none", "<Permission>");
                        case "setpath" -> this.pathHandler.getPaths().keySet().stream().toList();
                        default -> Collections.emptyList();
                    };
                }
            }
        }

        return Collections.emptyList();
    }
}