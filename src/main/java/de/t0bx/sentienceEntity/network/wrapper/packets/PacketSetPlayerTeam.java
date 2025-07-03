package de.t0bx.sentienceEntity.network.wrapper.packets;

import de.t0bx.sentienceEntity.network.utils.PacketId;
import de.t0bx.sentienceEntity.network.utils.PacketUtils;
import de.t0bx.sentienceEntity.network.utils.TeamMethods;
import de.t0bx.sentienceEntity.network.version.ProtocolVersion;
import de.t0bx.sentienceEntity.network.version.VersionRegistry;
import de.t0bx.sentienceEntity.network.version.registries.PacketIdRegistry;
import de.t0bx.sentienceEntity.network.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

@AllArgsConstructor
public class PacketSetPlayerTeam implements PacketWrapper {

    private final String teamName;
    private final TeamMethods teamMethods;
    private final byte friendlyFireFlags;
    private final String nameTagVisibility;
    private final String collisionRule;
    private final int color;
    private final List<String> entities;
    private final int packetId = PacketIdRegistry.getPacketId(PacketId.SET_PLAYER_TEAM);

    @Override
    public ByteBuf build() {
        ByteBuf buf = Unpooled.buffer();

        PacketUtils.writeVarInt(buf, packetId);

        try {
            PacketUtils.writeString(buf, teamName);
            buf.writeByte(teamMethods.getId());

            PacketUtils.writeComponent(buf, MiniMessage.miniMessage().deserialize("<green>Displayname"));
            buf.writeByte(friendlyFireFlags);

            if (VersionRegistry.getVersion().getProtocolId() == ProtocolVersion.V1_21_4.getProtocolId()) {
                PacketUtils.writeString(buf, nameTagVisibility);
                PacketUtils.writeString(buf, collisionRule);
            } else {
                PacketUtils.writeVarInt(buf, 1);
                PacketUtils.writeVarInt(buf, 1);
            }

            PacketUtils.writeVarInt(buf, color);

            PacketUtils.writeComponent(buf, MiniMessage.miniMessage().deserialize("<green>Prefix"));
            PacketUtils.writeComponent(buf, MiniMessage.miniMessage().deserialize("<green>Suffix"));

            PacketUtils.writeVarInt(buf, entities.size());
            for (String entity : entities) {
                PacketUtils.writeString(buf, entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return buf;
    }
}
