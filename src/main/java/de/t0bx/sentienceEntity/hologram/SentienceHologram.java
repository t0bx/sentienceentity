/**
 * SentienceEntity API License v1.1
 * Copyright (c) 2025 (t0bx)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to use, copy, modify, and integrate the Software into their own projects, including commercial and closed-source projects, subject to the following conditions:
 * <p>
 * 1. Attribution:
 * You must give appropriate credit to the original author ("Tobias Schuster" or "t0bx"), provide a link to the source or official page if available, and indicate if changes were made. You must do so in a reasonable and visible manner, such as in your plugin.yml, README, or about page.
 * <p>
 * 2. No Redistribution or Resale:
 * You may NOT sell, redistribute, or otherwise make the original Software or modified standalone versions of it available as a product (free or paid), plugin, or downloadable file, unless you have received prior written permission from the author. This includes publishing the plugin on any marketplace (e.g., SpigotMC, MC-Market, Polymart) or including it in paid bundles.
 * <p>
 * 3. Use as Dependency/API:
 * You are allowed to use this Software as a dependency or library in your own plugin or project, including in paid products, as long as attribution is given and the Software itself is not being sold or published separately.
 * <p>
 * 4. No Misrepresentation:
 * You may not misrepresent the origin of the Software. You must clearly distinguish your own modifications from the original work. The original author's name may not be removed from the source files or documentation.
 * <p>
 * 5. License Retention:
 * This license notice and all conditions must be preserved in all copies or substantial portions of the Software.
 * <p>
 * 6. Disclaimer:
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY ARISING FROM THE USE OF THIS SOFTWARE.
 * <p>
 * ---
 * <p>
 * Summary (non-binding):
 * You may use this plugin in your projects, even commercially, but you may not resell or republish it. Always give credit to t0bx.
 */

package de.t0bx.sentienceEntity.hologram;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.boundingbox.BoundingBoxRegistry;
import de.t0bx.sentienceEntity.network.PacketPlayer;
import de.t0bx.sentienceEntity.network.metadata.MetadataEntry;
import de.t0bx.sentienceEntity.network.metadata.MetadataType;
import de.t0bx.sentienceEntity.network.wrapper.packets.*;
import de.t0bx.sentienceEntity.utils.ReflectionUtils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
public class SentienceHologram {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final double height;
    private final double LINE_HEIGHT = 0.25;
    private final double ITEM_HEIGHT = 0.8;

    private final int entityId;
    private final UUID uuid;

    @Setter
    private Location baseLocation;
    private final Map<Integer, HologramLine> hologramLines;

    @Setter
    private Location location;

    private final Set<PacketPlayer> channels = new HashSet<>();

    /**
     * Constructs a new SentienceHologram with the specified entity ID, unique identifier,
     * and base location. A SentienceHologram represents a multi-line holographic display
     * that can be spawned at a given location.
     *
     * @param entityId     the entity ID associated with the hologram
     * @param uuid         the unique identifier of the hologram
     * @param baseLocation the base location at which the hologram will be displayed
     */
    public SentienceHologram(int entityId, EntityType entityType, UUID uuid, Location baseLocation) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.height = BoundingBoxRegistry.getBoundingBox(entityType).height();
        this.baseLocation = baseLocation;

        this.hologramLines = new HashMap<>();
        this.location = baseLocation.clone();
    }

    /**
     * Adds a new line to the hologram display. The line will be positioned below the
     * existing lines in the hologram, taking into account the vertical line spacing.
     *
     * @param line the text content of the new hologram line to be added
     */
    public void addLine(String line) {
        int lineIndex = hologramLines.size();
        int lineEntityId = ReflectionUtils.generateValidMinecraftEntityId();

        HologramLine hologramLine = new HologramLine(lineEntityId, this.uuid);
        hologramLine.setText(line);
        hologramLines.put(lineIndex, hologramLine);

        Location lineLocation = this.baseLocation.clone();
        lineLocation.add(0, calculateYOffset(lineIndex), 0);
        hologramLine.setLocation(lineLocation);

        spawnLine(hologramLine);

        updateAllLinePositions();
    }

    /**
     * Adds a new item-based line to the hologram display. The line will be displayed
     * below the existing lines in the hologram, considering the vertical line spacing.
     *
     * @param itemStack the {@link ItemStack} to be displayed as the new hologram line
     */
    public void addLine(ItemStack itemStack) {
        int lineIndex = hologramLines.size();
        int lineEntityId = ReflectionUtils.generateValidMinecraftEntityId();

        HologramLine hologramLine = new HologramLine(lineEntityId, this.uuid);
        hologramLine.setItemStack(itemStack);
        hologramLines.put(lineIndex, hologramLine);

        Location lineLocation = this.baseLocation.clone();
        lineLocation.add(0, calculateYOffset(lineIndex), 0);
        hologramLine.setLocation(lineLocation);

        spawnLine(hologramLine);

        updateAllLinePositions();
    }

    /**
     * Spawns a hologram line at its designated location and sends the appropriate entity
     * and metadata packets to all connected players. This method handles both text-based
     * and item-based hologram lines, creating the respective entity types and associating
     * the necessary metadata.
     *
     * @param line the {@link HologramLine} to be spawned. This line contains the entity ID,
     *             location, and other attributes needed to render the line in the hologram.
     */
    private void spawnLine(HologramLine line) {
        Location location = line.getLocation();
        List<MetadataEntry> metadataEntries = new ArrayList<>();
        metadataEntries.add(new MetadataEntry(0, MetadataType.BYTE, (byte) 32));
        metadataEntries.add(new MetadataEntry(5, MetadataType.BOOLEAN, true));

        if (line.getItemStack() == null) {
            var addEntityPacket = new PacketSpawnEntity(
                    line.getEntityId(),
                    UUID.randomUUID(),
                    EntityType.ARMOR_STAND,
                    location,
                    location.getYaw(),
                    0,
                    (short) 0,
                    (short) 0,
                    (short) 0
            );

            for (PacketPlayer player : channels) {
                player.sendPacket(addEntityPacket);
            }


            Component component = MiniMessage.miniMessage().deserialize(line.getText());
            metadataEntries.add(new MetadataEntry(15, MetadataType.BYTE, (byte) 25));
            metadataEntries.add(new MetadataEntry(2, MetadataType.OPTIONAL_TEXT_COMPONENT, Optional.of(component)));
            metadataEntries.add(new MetadataEntry(3, MetadataType.BOOLEAN, true));

            var metadata = new PacketSetEntityMetadata(line.getEntityId(), metadataEntries);

            for (PacketPlayer player : channels) {
                player.sendPacket(metadata);
            }
            return;
        }

        var addEntityPacket = new PacketSpawnEntity(
                line.getEntityId(),
                UUID.randomUUID(),
                EntityType.ITEM,
                location,
                location.getYaw(),
                0,
                (short) 0,
                (short) 0,
                (short) 0
        );

        for (PacketPlayer player : channels) {
            player.sendPacket(addEntityPacket);
        }

        metadataEntries.add(new MetadataEntry(8, MetadataType.SLOT, line.getItemStack()));

        var metadata = new PacketSetEntityMetadata(line.getEntityId(), metadataEntries);

        for (PacketPlayer player : channels) {
            player.sendPacket(metadata);
        }
    }

    /**
     * Removes a hologram line from the hologram display based on its index.
     * This method updates any internal structures and notifies connected players
     * to remove the corresponding hologram line entity from their view.
     *
     * @param index the index of the hologram line to be removed
     */
    public void removeLine(int index) {
        if (!hologramLines.containsKey(index)) return;

        HologramLine line = hologramLines.remove(index);
        if (line == null) return;

        var removeEntityPacket = new PacketRemoveEntities(List.of(line.getEntityId()));
        for (PacketPlayer player : channels) {
            player.sendPacket(removeEntityPacket);
        }

        updateLinesAfterRemoval();
    }

    /**
     * Updates the location of the hologram and all its associated lines, repositioning them
     * based on the provided base location. This method recalculates and assigns new positions
     * for each hologram line, taking into account their order and vertical spacing, and
     * sends teleport packets to update all connected players.
     *
     * @param location the new base {@link Location} to which the hologram and its lines
     *                 will be moved
     */
    public void updateLocation(Location location) {
        this.setBaseLocation(location);
        this.location = location.clone();
        updateAllLinePositions();
    }

    /**
     * Updates the positions of all hologram lines relative to the base location.
     * This method iterates through all hologram lines, recalculates each line's
     * vertical offset using their indices, and adjusts their locations accordingly.
     * For each updated location, a teleport packet is sent to all connected players
     * to synchronize the hologram's display in real-time.
     *
     * The lines are processed in order based on their indices, ensuring the proper
     * vertical stacking of lines within the hologram structure.
     */
    private void updateAllLinePositions() {
        List<Map.Entry<Integer, HologramLine>> sortedLines = new ArrayList<>(hologramLines.entrySet());
        sortedLines.sort(Map.Entry.comparingByKey());

        for (int i = 0; i < sortedLines.size(); i++) {
            Map.Entry<Integer, HologramLine> entry = sortedLines.get(i);
            HologramLine line = entry.getValue();

            Location newLocation = baseLocation.clone();
            newLocation.add(0, calculateYOffset(i), 0);

            line.setLocation(newLocation);

            var teleportPacket = new PacketTeleportEntity(
                    line.getEntityId(),
                    newLocation,
                    0, 0, 0,
                    true
            );

            for (PacketPlayer player : channels) {
                player.sendPacket(teleportPacket);
            }
        }
    }

    /**
     * Updates the indices of hologram lines in the internal data structure after a line
     * has been removed. This method ensures the order of indices is contiguous, starting
     * from zero, and reassigns each line to its new index. Once the reorganization is
     * complete, it triggers an update of all line positions to reflect the changes.
     */
    private void updateLinesAfterRemoval() {
        List<Map.Entry<Integer, HologramLine>> linesList = new ArrayList<>(hologramLines.entrySet());
        linesList.sort(Comparator.comparingInt(Map.Entry::getKey));

        hologramLines.clear();

        for (int newIndex = 0; newIndex < linesList.size(); newIndex++) {
            HologramLine line = linesList.get(newIndex).getValue();
            hologramLines.put(newIndex, line);
        }

        updateAllLinePositions();
    }

    /**
     * Calculates the vertical offset (Y offset) for a hologram line at a specific index.
     * The offset is determined based on the cumulative height of lines below the specified index
     * and half the height of the current line. Each line's height depends on whether it contains
     * an ItemStack or only text.
     *
     * @param currentIndex the index of the hologram line for which the Y offset is being calculated
     * @return the calculated vertical Y offset for the specified hologram line
     */
    private double calculateYOffset(int currentIndex) {
        List<Map.Entry<Integer, HologramLine>> sortedLines = new ArrayList<>(hologramLines.entrySet());
        sortedLines.sort(Map.Entry.comparingByKey());

        double heightBelow = 0.0;

        for (int i = currentIndex + 1; i < sortedLines.size(); i++) {
            HologramLine line = sortedLines.get(i).getValue();
            if (line.getItemStack() != null) {
                heightBelow += ITEM_HEIGHT;
            } else {
                heightBelow += LINE_HEIGHT;
            }
        }

        if (currentIndex < sortedLines.size()) {
            HologramLine currentLine = sortedLines.get(currentIndex).getValue();
            if (currentLine.getItemStack() != null) {
                heightBelow += ITEM_HEIGHT / 2;
            } else {
                heightBelow += LINE_HEIGHT / 2;
            }
        }

        return this.height + heightBelow;
    }

    /**
     * Updates the text of a specific hologram line identified by its index.
     * If the specified index does not exist or the hologram line is null,
     * the method returns without making any changes. Otherwise, the line's
     * text is updated, and the metadata associated with the hologram line
     * is sent to all connected players to reflect the update.
     *
     * @param index   the index of the hologram line to update
     * @param newText the new text to set for the specified hologram line
     */
    public void updateLineText(int index, String newText) {
        if (!hologramLines.containsKey(index)) return;

        HologramLine line = hologramLines.get(index);
        if (line == null) return;

        line.setText(newText);

        Component component = miniMessage.deserialize(line.getText());

        var metadata = new PacketSetEntityMetadata(line.getEntityId(), List.of(
                new MetadataEntry(2, MetadataType.OPTIONAL_TEXT_COMPONENT, Optional.of(component))
        ));

        for (PacketPlayer player : this.channels) {
            player.sendPacket(metadata);
        }
    }

    public void updateLineItemStack(int index, ItemStack newItemStack) {
        if (!hologramLines.containsKey(index)) return;

        HologramLine line = hologramLines.get(index);
        if (line == null) return;

        line.setItemStack(newItemStack);

        var metadata = new PacketSetEntityMetadata(line.getEntityId(), List.of(
                new MetadataEntry(8, MetadataType.SLOT, newItemStack)
        ));

        for (PacketPlayer player : this.channels) {
            player.sendPacket(metadata);
        }
    }

    /**
     * Spawns the hologram for the specified player. This includes initializing
     * and displaying all hologram lines at the designated location for the player.
     * If the player has already been marked as having viewed the hologram or if
     * the player is in a different world than the hologram's location, the method
     * exits without performing any action.
     *
     * @param player the {@link Player} who will view the hologram. The player's
     *               connection is used to associate the hologram lines through
     *               packet communication.
     */
    public void spawn(Player player) {
        PacketPlayer packetPlayer = SentienceEntity.getInstance().getPacketController().getPlayer(player);

        if (hasSpawned(packetPlayer)) return;
        if (!player.getWorld().getName().equals(this.getLocation().getWorld().getName())) return;

        channels.add(packetPlayer);

        for (HologramLine line : hologramLines.values()) {
            spawnLine(line);
        }
    }

    /**
     * Despawns the hologram for the specified player. This method ensures that
     * all the hologram lines associated with the hologram are removed from the
     * player's view by sending packets to destroy the hologram entities.
     * If the player has not previously spawned the hologram, this method exits without action.
     *
     * @param player the {@link Player} for whom the hologram will be despawned.
     *               The player's associated {@link PacketPlayer} is used
     *               to send destroy entity packets.
     */
    public void despawn(Player player) {
        PacketPlayer packetPlayer = SentienceEntity.getInstance().getPacketController().getPlayer(player);

        if (!hasSpawned(packetPlayer)) return;

        for (HologramLine line : hologramLines.values()) {
            var destroyEntity = new PacketRemoveEntities(List.of(line.getEntityId()));
            packetPlayer.sendPacket(destroyEntity);
        }

        channels.remove(packetPlayer);
    }

    /**
     * Destroys the hologram by removing all its lines and clearing associated data structures.
     * <p>
     * This method iterates through all hologram lines and sends packets to the connected players
     * instructing them to remove the entity associated with each line. After notifying all players,
     * it clears the list of connected channels and hologram lines, effectively cleaning up resources
     * related to this hologram.
     * <p>
     * It ensures that players no longer see any remnants of the hologram and that its internal
     * structures are reset for potential reuse or disposal.
     */
    public void destroy() {
        for (HologramLine line : hologramLines.values()) {
            var destroyEntity = new PacketRemoveEntities(List.of(line.getEntityId()));
            for (PacketPlayer player : channels) {
                player.sendPacket(destroyEntity);
            }
        }
        channels.clear();
        hologramLines.clear();
    }

    /**
     * Checks if the specified player has already spawned the hologram.
     * This method determines if the player is included in the collection
     * of channels that track players who have spawned the hologram.
     *
     * @param player the {@link PacketPlayer} to check for hologram spawning status
     * @return {@code true} if the player has spawned the hologram, {@code false} otherwise
     */
    public boolean hasSpawned(PacketPlayer player) {
        return this.channels.contains(player);
    }
}