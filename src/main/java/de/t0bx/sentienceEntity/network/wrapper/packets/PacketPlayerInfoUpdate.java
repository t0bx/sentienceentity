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

package de.t0bx.sentienceEntity.network.wrapper.packets;

import de.t0bx.sentienceEntity.network.utils.PacketId;
import de.t0bx.sentienceEntity.network.utils.PacketUtils;
import de.t0bx.sentienceEntity.network.version.registries.PacketIdRegistry;
import de.t0bx.sentienceEntity.network.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

public class PacketPlayerInfoUpdate implements PacketWrapper {

    /**
     * The {@code Action} enum represents different types of player-related updates
     * that can be applied in a multiplayer server context. These updates are used
     * in player information packets for tasks such as adding players, updating their
     * game mode, or managing their visibility and display properties.
     *
     * Each action is associated with a specific bit mask to enable combining multiple
     * actions into a single operation using bitwise operations.
     *
     * Enum Constants:
     * - {@code ADD_PLAYER}: Adds a new player to the player list.
     * - {@code INITIALIZE_CHAT}: Initializes chat-related settings for a player.
     * - {@code UPDATE_GAME_MODE}: Updates the game mode of a player.
     * - {@code UPDATE_LISTED}: Toggles the visibility of a player in the player list.
     * - {@code UPDATE_LATENCY}: Updates the latency/ping value of a player.
     * - {@code UPDATE_DISPLAY_NAME}: Updates the display name of a player.
     * - {@code UPDATE_LIST_PRIORITY}: Adjusts the priority of a player in the player list.
     * - {@code UPDATE_HAT}: Updates a player's hat appearance or related properties.
     */
    public enum Action {
        ADD_PLAYER(0x01),
        INITIALIZE_CHAT(0x02),
        UPDATE_GAME_MODE(0x04),
        UPDATE_LISTED(0x08),
        UPDATE_LATENCY(0x10),
        UPDATE_DISPLAY_NAME(0x20),
        UPDATE_LIST_PRIORITY(0x40),
        UPDATE_HAT(0x80);

        public final int bit;

        Action(int bit) {
            this.bit = bit;
        }
    }

    /**
     * Represents a property that contains a name, value, and signature.
     * Properties are typically used to store metadata or attributes
     * associated with entities or objects in a networked context.
     *
     * @param name the name of the property, identifying its purpose or type.
     * @param value the value associated with the property, representing its data.
     * @param signature the signature validating or authenticating the property.
     */
    public record Property(String name, String value, String signature) {
    }

    /**
     * Represents an entry of a player used within the context of a Player Info Update packet.
     * This class contains all necessary information about a player such as their UUID, name,
     * properties, latency, and visibility status.
     *
     * Fields:
     * - uuid: A universally unique identifier (UUID) representing the player.
     * - name: The name of the player as a string.
     * - properties: A list of Property objects associated with the player, often used for
     *   metadata (e.g., skins or other properties visible on the client).
     * - latency: The player's latency in milliseconds, typically representing their
     *   connection speed or server response time.
     * - listed: A boolean indicating whether the player is listed (visible) on the server's
     *   player list or not.
     */
    @AllArgsConstructor
    public static class PlayerEntry {
        public UUID uuid;
        public String name;
        public List<Property> properties;
        public int latency;
        public boolean listed;
    }

    private final List<Action> actions;
    private final List<PlayerEntry> entries;
    private final int packetId = PacketIdRegistry.getPacketId(PacketId.PLAYER_INFO_UPDATE);

    /**
     * Constructs a new {@code PacketPlayerInfoUpdate} instance for updating player information
     * in a multiplayer server context. This packet is used to send updates related to the players,
     * such as adding a player, updating their latency, or setting their visibility status.
     *
     * @param actions a list of {@code Action} enums representing the types of updates to be applied
     *                to the player entries (e.g., adding a player, updating latency, etc.).
     * @param entries a list of {@code PlayerEntry} objects representing the players for which the
     *                updates specified in the {@code actions} list will be applied.
     */
    public PacketPlayerInfoUpdate(List<Action> actions, List<PlayerEntry> entries) {
        this.actions = actions;
        this.entries = entries;
    }

    /**
     * Serializes and builds a {@code ByteBuf} representing the Player Info Update packet.
     * This method constructs the packet by applying actions to a list of player entries,
     * which may include adding players, updating their latency, visibility, or other properties.
     * It writes data such as packet ID, action mask, and player-specific details to the buffer,
     * in the correct format for network transmission.
     *
     * @return a {@code ByteBuf} containing the serialized Player Info Update packet data.
     */
    @Override
    public ByteBuf build() {
        ByteBuf buf = Unpooled.buffer();

        PacketUtils.writeVarInt(buf, packetId);
        int mask = actions.stream().mapToInt(action -> action.bit).reduce(0, (a, b) -> a | b);
        PacketUtils.writeVarInt(buf, mask);

        PacketUtils.writeVarInt(buf, entries.size());
        for (PlayerEntry entry : entries) {
            PacketUtils.writeUUID(buf, entry.uuid);

            for (Action action : actions) {
                switch (action) {
                    case ADD_PLAYER -> {
                        PacketUtils.writeString(buf, entry.name);

                        PacketUtils.writeVarInt(buf, entry.properties.size());
                        for (Property property : entry.properties) {
                            PacketUtils.writeString(buf, property.name);
                            PacketUtils.writeString(buf, property.value);
                            boolean hasSignature = property.signature != null;
                            PacketUtils.writeBoolean(buf, hasSignature);
                            if (hasSignature) {
                                PacketUtils.writeString(buf, property.signature);
                            }
                        }
                    }
                    case UPDATE_LATENCY -> PacketUtils.writeVarInt(buf, entry.latency);
                    case UPDATE_LISTED -> PacketUtils.writeBoolean(buf, entry.listed);
                    case UPDATE_DISPLAY_NAME -> PacketUtils.writeBoolean(buf, false);
                    default -> {
                    }
                }
            }
        }

        return buf;
    }
}
