/**
 * Creative Commons Attribution-NonCommercial 4.0 International Public License
 * By using this code, you agree to the following terms:
 * You are free to:
 * - Share — copy and redistribute the material in any medium or format
 * - Adapt — remix, transform, and build upon the material
 * Under the following terms:
 * 1. Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made.
 * 2. NonCommercial — You may not use the material for commercial purposes.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 * Full License Text: https://creativecommons.org/licenses/by-nc/4.0/legalcode
 * ---
 * Copyright (c) 2025 t0bx
 * This work is licensed under the Creative Commons Attribution-NonCommercial 4.0 International License.
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

    public PacketReceiveHandler(NpcsHandler npcsHandler, PacketController packetController) {
        this.npcsHandler = npcsHandler;
        this.packetController = packetController;
    }

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
