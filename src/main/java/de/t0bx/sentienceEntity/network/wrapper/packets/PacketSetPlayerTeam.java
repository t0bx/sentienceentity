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

    /**
     * Constructs a {@link ByteBuf} representing the packet for setting player team data.
     * This method encodes the necessary fields of the team such as team name, methods, attributes,
     * components, and entity list into the packet buffer.
     *
     * @return A {@link ByteBuf} containing the encoded packet data for setting player team.
     */
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
