package de.t0bx.sentienceEntity.commands;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.hologram.HologramLine;
import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.npc.NpcsHandler;
import de.t0bx.sentienceEntity.npc.SentienceNPC;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.t0bx.sentienceEntity.utils.MessageUtils.sendMessage;

public class SentienceHologramCommand implements CommandExecutor, TabCompleter {

    private final MiniMessage miniMessage;
    private final String prefix;
    private final HologramManager hologramManager;
    private final NpcsHandler npcsHandler;

    public SentienceHologramCommand(SentienceEntity sentienceEntity) {
        this.miniMessage = MiniMessage.miniMessage();
        this.prefix = sentienceEntity.getPrefix();
        this.hologramManager = sentienceEntity.getHologramManager();
        this.npcsHandler = sentienceEntity.getNpcshandler();
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        if (!player.hasPermission("se.hologram")) {
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
            case "createhologram" -> {
                if (args.length != 2) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh createHologram <Name> <dark_gray>| <gray>Create a Hologram for a npc"));
                    return true;
                }

                String npcName = args[1];
                this.handleCreateHologram(player, npcName);
            }

            case "addtextline" -> {
                if (args.length <= 2) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh addTextLine <Name> <Text> <dark_gray>| <gray>Add a Text line for a hologram"));
                    return true;
                }

                String npcName = args[1];
                String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                this.handleAddLine(player, npcName, text);
            }

            case "additemline" -> {
                if (args.length != 2) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh addItemLine <Name> <dark_gray>| <gray>Add a Item line for a hologram"));
                    return true;
                }

                String npcName = args[1];
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                if (itemStack.getType() == Material.AIR) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "You need to hold an item in your hand!"));
                    return true;
                }

                this.handleAddLine(player, npcName, itemStack);
            }

            case "settextline" -> {
                if (args.length <= 3) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh setLine <Name> <index> <Text> <dark_gray>| <gray>Updates a specific Text line from a hologram"));
                    return true;
                }

                String npcName = args[1];
                int index;

                try {
                    index = Integer.parseInt(args[2]);
                } catch (NumberFormatException ignored) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "The index has to be a number!"));
                    return true;
                }

                String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                this.handleSetLine(player, npcName, index, text);
            }

            case "setitemline" -> {
                if (args.length != 3) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh setItemLine <Name> <index> <dark_gray>| <gray>Updates a specific Item line from a hologram"));
                    return true;
                }

                String npcName = args[1];
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                if (itemStack.getType() == Material.AIR) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "You need to hold an item in your hand!"));
                    return true;
                }

                int index;

                try {
                    index = Integer.parseInt(args[2]);
                } catch (NumberFormatException ignored) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "The index has to be a number!"));
                    return true;
                }

                this.handleSetLine(player, npcName, index, itemStack);
            }

            case "lines" -> {
                if (args.length != 2) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh lines <Name> <dark_gray>| <gray>List all lines from a hologram"));
                    return true;
                }

                String npcName = args[1];
                this.handleHologramLines(player, npcName);
            }

            case "removeline" -> {
                if (args.length != 3) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh removeLine <Name> <index> <dark_gray>| <gray>Removes a specific line from a hologram"));
                    return true;
                }

                String npcName = args[1];
                int index = Integer.parseInt(args[2]);
                this.handleRemoveLine(player, npcName, index);
            }

            case "removehologram" -> {
                if (args.length != 2) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh removeHologram <Name> <dark_gray>| <gray>Removes a hologram"));
                    return true;
                }

                String npcName = args[1];
                this.handleRemoveHologram(player, npcName);
            }

            default -> sendHelp(player);
        }

        return false;
    }

    private void handleCreateHologram(Player player, String npcName) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        SentienceNPC npc = this.npcsHandler.getNPC(npcName);
        this.hologramManager.createHologram(npcName);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "You have created a hologram for the npc '" + npcName + "'."));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Use /sh addTextLine to add a Text line to the hologram!"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Use /sh addItemLine to add a Item line to the hologram!"));
    }

    private void handleAddLine(Player player, String npcName, String text) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        if (this.hologramManager.getHologram(npcName) == null) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no hologram for the npc '" + npcName + "'! Use /sh createHologram to create a hologram first!"));
            return;
        }

        this.hologramManager.show(player, npcName);
        this.hologramManager.addLine(npcName, text, true);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "You have added a line to the hologram for the npc '" + npcName + "'."));
    }

    private void handleAddLine(Player player, String npcName, ItemStack itemStack) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        if (this.hologramManager.getHologram(npcName) == null) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no hologram for the npc '" + npcName + "'! Use /sh createHologram to create a hologram first!"));
            return;
        }

        this.hologramManager.show(player, npcName);
        this.hologramManager.addLine(npcName, itemStack, true);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "You have added a item to the hologram for the npc '" + npcName + "'."));
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

        this.hologramManager.updateLineText(npcName, index, text);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "You've changed the line '" + index + "' to '" + text + "'!"));
    }

    private void handleSetLine(Player player, String npcName, int index, ItemStack itemStack) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        if (!this.hologramManager.doesLineExist(npcName, index)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no line with the index '" + index + "'!"));
            return;
        }

        this.hologramManager.updateLineItemStack(npcName, index, itemStack);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "You've changed the line '" + index + "' to '" + itemStack.getType().name() + "'!"));
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

        if (this.hologramManager.getHologram(npcName) == null) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no hologram for the npc '" + npcName + "'! Use /sh createHologram to create a hologram first!"));
            return;
        }

        Map<Integer, HologramLine> hologramLines = this.hologramManager.getHologramLines(npcName);
        if (hologramLines.isEmpty()) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There are no HologramLines for the npc '" + npcName + "'!"));
            return;
        }

        hologramLines.forEach((index, hologramline) -> {
            if (hologramline.getItemStack() != null) {
                sendMessage(player, this.miniMessage.deserialize(this.prefix + "Index: '" + index + "', Item: '" + hologramline.getItemStack().getType().name() + "'"));
            } else {
                sendMessage(player, this.miniMessage.deserialize(this.prefix + "Index: '" + index + "', Text: '" + hologramline.getText() + "'"));
            }
        });
    }

    private void handleRemoveHologram(Player player, String npcName) {
        if (!this.npcsHandler.doesNPCExist(npcName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no npc with the name '" + npcName + "'!"));
            return;
        }

        if (this.hologramManager.getHologram(npcName) == null) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "There is no hologram for the npc '" + npcName + "'! Use /sh createHologram to create a hologram first!"));
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
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh createHologram <Name> <dark_gray>| <gray>Create a Hologram for a npc"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh addTextLine <Name> <Text> <dark_gray>| <gray>Add a Text line for a hologram"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh addItemLine <Name> <dark_gray>| <gray>Add a Item line for a hologram"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh setTextLine <Name> <index> <Text> <dark_gray>| <gray>Updates a specific Text line from a hologram"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh setItemLine <Name> <index> <dark_gray>| <gray>Updates a specific Item line from a hologram"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh lines <Name> <dark_gray>| <gray>List all lines from a hologram"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh removeLine <Name> <index> <dark_gray>| <gray>Removes a specific line from a hologram"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sh removeHologram <Name> <dark_gray>| <gray>Removes a hologram"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return List.of("createHologram", "addTextLine", "addItemLine", "setTextLine", "setItemLine", "lines", "removeLine", "removeHologram");
        }

        List<String> npcNames = this.npcsHandler.getNPCNames();
        switch (args[0].toLowerCase()) {
            case "createhologram", "removehologram", "lines" -> {
                return npcNames;
            }

            case "addtextline" -> {
                if (args.length == 2) {
                    return npcNames;
                }

                if (args.length == 3) {
                    return Collections.singletonList("<Text>");
                }
            }

            case "additemline" -> {
                if (args.length == 2) {
                    return npcNames;
                }
            }

            case "settextline" -> {
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

            case "setitemline" -> {
                if (args.length == 2) {
                    return npcNames;
                }

                if (args.length == 3) {
                    if (this.hologramManager.getHologramLines(args[1]) == null) return Collections.emptyList();

                    return this.hologramManager.getHologramLines(args[1]).keySet().stream().map(String::valueOf).collect(Collectors.toList());
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
