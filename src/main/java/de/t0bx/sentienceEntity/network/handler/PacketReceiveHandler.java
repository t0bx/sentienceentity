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

package de.t0bx.sentienceEntity.network.handler;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.events.PlayerClickNpcEvent;
import de.t0bx.sentienceEntity.network.PacketController;
import de.t0bx.sentienceEntity.network.version.registries.PacketIdRegistry;
import de.t0bx.sentienceEntity.npc.NpcsHandler;
import de.t0bx.sentienceEntity.network.interact.InteractHand;
import de.t0bx.sentienceEntity.network.interact.InteractType;
import de.t0bx.sentienceEntity.network.utils.PacketId;
import de.t0bx.sentienceEntity.network.utils.PacketUtils;
import de.t0bx.sentienceEntity.network.wrapper.packets.PacketInteractEntity;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class PacketReceiveHandler {

    private final NpcsHandler npcsHandler;
    private final PacketController packetController;

    /**
     * Constructs a new PacketReceiveHandler.
     *
     * @param npcsHandler the handler responsible for managing NPC-related logic
     * @param packetController the controller responsible for managing packets
     */
    public PacketReceiveHandler(NpcsHandler npcsHandler, PacketController packetController) {
        this.npcsHandler = npcsHandler;
        this.packetController = packetController;
    }

    /**
     * Injects a {@link Player} into the packet interception system, enabling custom handling
     * of specific inbound packets for that player. This method modifies the player's channel
     * pipeline by adding a custom {@link ByteToMessageDecoder} to intercept packets.
     * If a custom decoder already exists for the player, it is removed before adding the new one.
     *
     * @param player the {@link Player} to inject into the packet interception system
     */
    public void injectPlayer(Player player) {
        try {
            Channel channel = this.packetController.getPlayer(player).getChannel();
            String inboundHandlerName = "packet_interceptor_in_" + player.getName();

            if (channel.pipeline().get(inboundHandlerName) != null) {
                channel.pipeline().remove(inboundHandlerName);
            }

            channel.pipeline().addBefore("decoder", inboundHandlerName,
                    new ByteToMessageDecoder() {
                        @Override
                        protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) {
                            byteBuf.markReaderIndex();
                            if (!byteBuf.isReadable()) return;

                            boolean handeld = false;
                            int packetId;
                            try {
                                packetId = PacketUtils.readVarInt(byteBuf);
                            } catch (Exception exception) {
                                byteBuf.resetReaderIndex();
                                out.add(byteBuf.readBytes(byteBuf.readableBytes()));
                                return;
                            }

                            if (packetId == PacketIdRegistry.getPacketId(PacketId.INTERACT_ENTITY)) {
                                try {
                                    PacketInteractEntity packet = PacketInteractEntity.read(byteBuf);
                                    InteractType type = packet.getInteractType();
                                    InteractHand hand = packet.getHand().orElse(null);

                                    if (type != InteractType.ATTACK && hand == null) return;

                                    int entityId = packet.getEntityId();
                                    if (!npcsHandler.getNpcIds().contains(entityId)) return;

                                    String npcName = npcsHandler.getNpcNameFromId(entityId);
                                    if (npcName == null) return;

                                    Player player = SentienceEntity.getInstance().getPacketController().getPlayer(ctx.channel());
                                    if (player == null) return;

                                    Bukkit.getScheduler().runTask(SentienceEntity.getInstance(), () -> {
                                        PlayerClickNpcEvent event = new PlayerClickNpcEvent(player, npcName, hand, type);
                                        Bukkit.getPluginManager().callEvent(event);
                                    });
                                    handeld = true;
                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                    byteBuf.resetReaderIndex();
                                }
                            }

                            if (!handeld) {
                                byteBuf.resetReaderIndex();
                                out.add(byteBuf.readBytes(byteBuf.readableBytes()));
                            }
                        }
                    });
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Removes the specified {@link Player} from the packet interception system, disabling
     * custom handling of inbound packets for that player. This is achieved by removing
     * the associated custom {@link ByteToMessageDecoder} from the player's channel pipeline.
     *
     * @param player the {@link Player} to uninject from the packet interception system
     */
    public void uninjectPlayer(Player player) {
        try {
            Channel channel = this.packetController.getPlayer(player).getChannel();
            String inboundHandlerName = "packet_interceptor_in_" + player.getName();

            if (channel.pipeline().get(inboundHandlerName) != null) {
                channel.pipeline().remove(inboundHandlerName);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
