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

package de.t0bx.sentienceEntity.hologram;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.network.PacketPlayer;
import de.t0bx.sentienceEntity.network.metadata.MetadataEntry;
import de.t0bx.sentienceEntity.network.metadata.MetadataType;
import de.t0bx.sentienceEntity.network.utils.EntityType;
import de.t0bx.sentienceEntity.network.wrapper.packets.*;
import de.t0bx.sentienceEntity.utils.ReflectionUtils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class SentienceHologram {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final double LINE_HEIGHT = 0.25;

    private final int entityId;
    private final UUID uuid;

    private final Location baseLocation;
    private final Map<Integer, HologramLine> hologramLines;

    @Setter
    private Location location;

    private final Set<PacketPlayer> channels = new HashSet<>();

    /**
     * Constructs a new SentienceHologram with the specified entity ID, unique identifier,
     * and base location. A SentienceHologram represents a multi-line holographic display
     * that can be spawned at a given location.
     *
     * @param entityId the entity ID associated with the hologram
     * @param uuid the unique identifier of the hologram
     * @param baseLocation the base location at which the hologram will be displayed
     */
    public SentienceHologram(int entityId, UUID uuid, Location baseLocation) {
        this.entityId = entityId;
        this.uuid = uuid;
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
        Location lineLocation = this.baseLocation.clone();
        lineLocation.add(0, 1.8 - (LINE_HEIGHT * (lineIndex + 1)), 0);

        HologramLine hologramLine = new HologramLine(lineEntityId, this.uuid, line, lineLocation);
        hologramLines.put(lineIndex, hologramLine);

        spawnLine(hologramLine);
        moveExistingLinesUp();
    }

    private void spawnLine(HologramLine line) {
        Location location = line.getLocation();
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

        var metadata = new PacketSetEntityMetadata(line.getEntityId(), List.of(
                new MetadataEntry(0, MetadataType.BYTE, (byte) 32),
                new MetadataEntry(2, MetadataType.OPTIONAL_TEXT_COMPONENT, Optional.of(component)),
                new MetadataEntry(3, MetadataType.BOOLEAN, true),
                new MetadataEntry(5, MetadataType.BOOLEAN, true),
                new MetadataEntry(15, MetadataType.BYTE, (byte) 25)
        ));

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

    private void updateLinesAfterRemoval() {
        List<Map.Entry<Integer, HologramLine>> linesList = new ArrayList<>(hologramLines.entrySet());

        linesList.sort(Comparator.comparingInt(Map.Entry::getKey));

        hologramLines.clear();

        for (int newIndex = 0; newIndex < linesList.size(); newIndex++) {
            HologramLine line = linesList.get(newIndex).getValue();

            Location newLocation = baseLocation.clone();
            newLocation.add(0, 1.8 + (LINE_HEIGHT * (linesList.size() - newIndex - 1)), 0);

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

            hologramLines.put(newIndex, line);
        }
    }

    private void moveExistingLinesUp() {
        List<HologramLine> linesList = new ArrayList<>(hologramLines.values());
        for (int i = 0; i < linesList.size(); i++) {
            HologramLine line = linesList.get(i);

            Location newLocation = baseLocation.clone();
            newLocation.add(0, 1.8 + (LINE_HEIGHT * (linesList.size() - i - 1)), 0);

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
     * Updates the text of a specific hologram line identified by its index.
     * If the specified index does not exist or the hologram line is null,
     * the method returns without making any changes. Otherwise, the line's
     * text is updated, and the metadata associated with the hologram line
     * is sent to all connected players to reflect the update.
     *
     * @param index the index of the hologram line to update
     * @param newText the new text to set for the specified hologram line
     */
    public void updateLine(int index, String newText) {
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
     *
     * This method iterates through all hologram lines and sends packets to the connected players
     * instructing them to remove the entity associated with each line. After notifying all players,
     * it clears the list of connected channels and hologram lines, effectively cleaning up resources
     * related to this hologram.
     *
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