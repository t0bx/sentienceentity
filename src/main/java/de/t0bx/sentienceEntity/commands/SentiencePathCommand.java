package de.t0bx.sentienceEntity.commands;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.npc.NpcsHandler;
import de.t0bx.sentienceEntity.npc.SentienceNPC;
import de.t0bx.sentienceEntity.path.SentiencePathHandler;
import de.t0bx.sentienceEntity.path.serializer.PathSerializer;
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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class SentiencePathCommand implements CommandExecutor, TabCompleter {

    private final MiniMessage miniMessage;
    private final String prefix;
    private final SentiencePathHandler sentiencePathHandler;
    private final NpcsHandler npcsHandler;

    public SentiencePathCommand(SentienceEntity sentienceEntity) {
        this.miniMessage = MiniMessage.miniMessage();
        this.prefix = sentienceEntity.getPrefix();
        this.sentiencePathHandler = sentienceEntity.getSentiencePathHandler();
        this.npcsHandler = sentienceEntity.getNpcshandler();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        if (!player.hasPermission("se.path")) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "You don't have permission to execute this command!"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length != 2) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sp create <pathname> <dark_gray>| <gray>Create a path with the given name"));
                    return true;
                }

                this.handleCreatePath(player, args[1]);
                return true;
            }

            case "remove" -> {
                if (args.length != 2) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sp remove <pathname> <dark_gray>| <gray>Remove a path with the given name"));
                    return true;
                }

                handleRemovePath(player, args[1]);
                return true;
            }

            case "addpoint" -> {
                if (args.length != 3) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sp addPoint <pathname> <walk/teleport> <dark_gray>| <gray>Add a point to a path with the given name"));
                    return true;
                }

                String pathName = args[1];
                String type = args[2];
                if (!type.equalsIgnoreCase("walk") && !type.equalsIgnoreCase("teleport")) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "The type '" + type + "' is not valid for a point! Valid types: walk, teleport"));
                    return true;
                }

                handleAddPoint(player, pathName, type.equalsIgnoreCase("teleport"));
                return true;
            }

            case "removepoint" -> {
                if (args.length != 3) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sp removePoint <pathname> <point index> <dark_gray>| <gray>Remove a point from a path with the given name"));
                    return true;
                }

                String pathName = args[1];
                try {
                    int index = Integer.parseInt(args[2]);
                    handleRemovePoint(player, pathName, index);
                } catch (NumberFormatException ignored) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "The index '" + args[2] + "' is not a valid number!"));
                }

                return true;
            }

            case "listpoints" -> {
                if (args.length != 2) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sp listPoints <pathname> <dark_gray>| <gray>List all points of a path with the given name"));
                    return true;
                }

                handleListPoints(player, args[1]);
                return true;
            }

            case "list" -> {
                if (args.length != 1) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sp list <dark_gray>| <gray>List all paths"));
                    return true;
                }

                handleListPaths(player);
                return true;
            }

            case "apply" -> {
                if (args.length != 3) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sp apply <pathname> <npcname> <dark_gray>| <gray>Apply the path to a npc"));
                    return true;
                }

                String pathName = args[1];
                if (!this.sentiencePathHandler.doesPathNameExist(pathName)) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "The path '" + pathName + "' does not exist!"));
                    return true;
                }

                String npcName = args[2];
                SentienceNPC npc = this.npcsHandler.getNPC(npcName);
                if (npc == null) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "The npc '" + npcName + "' does not exist!"));
                    return true;
                }

                this.sentiencePathHandler.applyPath(npc.getEntityId(), pathName);
                return true;
            }

            default -> sendHelp(player);
        }
        return false;
    }

    private void sendHelp(Player player) {
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sp create <pathname> <dark_gray>| <gray>Create a path with the given name"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sp remove <pathname> <dark_gray>| <gray>Remove a path with the given name"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sp addPoint <pathname> <walk/teleport> <dark_gray>| <gray>Add a point to a path with the given name"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sp removePoint <pathname> <point index> <dark_gray>| <gray>Remove a point from a path with the given name"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sp listPoints <pathname> <dark_gray>| <gray>List all points of a path with the given name"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sp list <dark_gray>| <gray>List all paths"));
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Usage: /sp apply <pathname> <npcname> <dark_gray>| <gray>Apply the path to a npc"));
    }

    private void handleCreatePath(Player player, String pathName) {
        if (this.sentiencePathHandler.doesPathNameExist(pathName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "A path with the name '" + pathName + "' already exists!"));
            return;
        }

        this.sentiencePathHandler.createPath(pathName);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Successfully created path '" + pathName + "'"));
    }

    private void handleRemovePath(Player player, String pathName) {
        if (!this.sentiencePathHandler.doesPathNameExist(pathName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "The path '" + pathName + "' does not exist!"));
            return;
        }

        try {
            if (PathSerializer.loadPath(pathName) != null) {
                PathSerializer.removePath(pathName);
            }
        } catch (IOException exception) {
            SentienceEntity.getInstance().getLogger().log(Level.WARNING, "Failed to remove path '" + pathName + "'!", exception);
        }

        this.sentiencePathHandler.removePath(pathName);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Successfully removed path '" + pathName + "'"));
    }

    private void handleAddPoint(Player player, String pathName, boolean isTeleport) {
        if (!this.sentiencePathHandler.doesPathNameExist(pathName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "The path '" + pathName + "' does not exist!"));
            return;
        }

        try {
            if (PathSerializer.loadPath(pathName) != null) {
                PathSerializer.removePath(pathName);
            }
        } catch (IOException exception) {
            SentienceEntity.getInstance().getLogger().log(Level.WARNING, "Failed to remove path '" + pathName + "'!", exception);
        }

        this.sentiencePathHandler.addPoint(pathName, player.getLocation(), isTeleport);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Successfully added point to path '" + pathName + "'"));
    }

    private void handleRemovePoint(Player player, String pathName, int index) {
        if (!this.sentiencePathHandler.doesPathNameExist(pathName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "The path '" + pathName + "' does not exist!"));
            return;
        }

        if (!this.sentiencePathHandler.hasPathIndex(pathName, index)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "The path '" + pathName + "' does not have a point at index '" + index + "'!"));
            return;
        }

        try {
            if (PathSerializer.loadPath(pathName) != null) {
                PathSerializer.removePath(pathName);
            }
        } catch (IOException exception) {
            SentienceEntity.getInstance().getLogger().log(Level.WARNING, "Failed to remove path '" + pathName + "'!", exception);
        }

        this.sentiencePathHandler.removePoint(pathName, index);
        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Successfully removed point from path '" + pathName + "'"));
    }

    private void handleListPoints(Player player, String pathName) {
        if (!this.sentiencePathHandler.doesPathNameExist(pathName)) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "The path '" + pathName + "' does not exist!"));
            return;
        }

        this.sentiencePathHandler.getPoints(pathName).forEach((index, point) -> {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Index: " + index +
                    " | X: " + String.format("%.3f", point.getLocation().getX()) +
                    " Y: " + point.getLocation().getY() +
                    " Z: " + String.format("%.3f", point.getLocation().getZ()) +
                    " Yaw: " + String.format("%.3f", point.getLocation().getYaw()) +
                    " Pitch: " + String.format("%.3f", point.getLocation().getPitch()) +
                    " | " + (point.isTeleport() ? "Teleport" : "Walk") + "."));
        });
    }

    private void handleListPaths(Player player) {
        if (this.sentiencePathHandler.getPaths().isEmpty()) {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "You don't have any paths created yet!"));
            return;
        }

        sendMessage(player, this.miniMessage.deserialize(this.prefix + "Paths:"));
        this.sentiencePathHandler.getPaths().forEach((pathName, sentiencePath) -> {
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Name: " + pathName + " | Points: " + sentiencePath.getPaths().size() + "."));
        });
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) return List.of();

        if (args.length == 1) {
            return List.of("create", "remove", "addPoint", "removePoint", "listPoints", "list", "apply");
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "create" -> {
                if (args.length == 2) {
                    yield List.of("<pathname>");
                }
                yield Collections.emptyList();
            }

            case "remove", "listpoints" -> {
                if (args.length == 2) {
                    yield this.sentiencePathHandler.getPaths().keySet().stream().toList();
                }
                yield Collections.emptyList();
            }

            case "addpoint" -> {
                if (args.length == 2) {
                    yield this.sentiencePathHandler.getPaths().keySet().stream().toList();
                } else if (args.length == 3) {
                    yield List.of("Walk", "Teleport");
                }
                yield Collections.emptyList();
            }

            case "removepoint" -> {
                if (args.length == 2) {
                    yield this.sentiencePathHandler.getPaths().keySet().stream().toList();
                } else if (args.length == 3) {
                    yield List.of("<pointIndex>");
                }
                yield Collections.emptyList();
            }

            case "apply" -> {
                if (args.length == 2) {
                    yield this.sentiencePathHandler.getPaths().keySet().stream().toList();
                } else if (args.length == 3) {
                    yield this.npcsHandler.getNPCNames();
                }
                yield Collections.emptyList();
            }

            default -> Collections.emptyList();
        };
    }

    private void sendMessage(Player player, Component component) {
        if (SentienceEntity.getInstance().isPAPER()) {
            player.sendMessage(component);
        } else {
            SentienceEntity.getInstance().getAudiences().player(player).sendMessage(component);
        }
    }
}
