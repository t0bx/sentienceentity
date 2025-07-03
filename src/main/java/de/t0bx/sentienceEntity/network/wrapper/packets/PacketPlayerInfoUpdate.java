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

    public record Property(String name, String value, String signature) {
    }

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
