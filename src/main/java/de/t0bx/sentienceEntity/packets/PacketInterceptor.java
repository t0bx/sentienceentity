/**
 *Creative Commons Attribution-NonCommercial 4.0 International Public License
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

package de.t0bx.sentienceEntity.packets;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.events.PlayerClickNPCEvent;
import de.t0bx.sentienceEntity.npc.NPCsHandler;
import de.t0bx.sentienceEntity.utils.ClickType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class PacketInterceptor {

    private Field entityIdField = null;
    private Field actionField = null;
    private Method getTypeMethod = null;
    private final NPCsHandler npCsHandler;

    public PacketInterceptor(NPCsHandler npCsHandler) {
        this.npCsHandler = npCsHandler;

        try {
            this.entityIdField = ServerboundInteractPacket.class.getDeclaredField("entityId");
            this.entityIdField.setAccessible(true);

            this.actionField = ServerboundInteractPacket.class.getDeclaredField("action");
            this.actionField.setAccessible(true);

            Class<?>[] innerClasses = ServerboundInteractPacket.class.getDeclaredClasses();
            for (Class<?> innerClass : innerClasses) {
                if (innerClass.getSimpleName().equals("Action")) {
                    getTypeMethod = innerClass.getDeclaredMethod("getType");
                    getTypeMethod.setAccessible(true);
                    break;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void injectPlayer(Player player) {
        try {
            Channel channel = this.getPlayerChannel(player);
            String inboundHandlerName = "packet_interceptor_in_" + player.getName();

            if (channel.pipeline().get(inboundHandlerName) != null) {
                channel.pipeline().remove(inboundHandlerName);
            }

            channel.pipeline().addBefore("packet_handler", inboundHandlerName,
                    new MessageToMessageDecoder<Packet<?>>() {
                        @Override
                        protected void decode(ChannelHandlerContext ctx, Packet<?> packet, List<Object> out) {
                            out.add(packet);
                            if (packet instanceof ServerboundInteractPacket interactPacket) {
                                try {
                                    int entityId = getEntityId(interactPacket);
                                    if (!npCsHandler.getNpcIds().contains(entityId)) return;

                                    InteractionHand interactionHand = getInteractionHand(interactPacket);
                                    if (interactionHand != InteractionHand.MAIN_HAND) return;

                                    String actionType = getActionType(interactPacket).toLowerCase();
                                    ClickType clickType = switch (actionType) {
                                        case "interact" -> ClickType.RIGHT_CLICK;
                                        case "attack" -> ClickType.LEFT_CLICK;
                                        default -> null;
                                    };
                                    if (clickType == null) return;

                                    String npcName = npCsHandler.getNpcNameFromId(entityId);
                                    if (npcName == null) return;
                                    Bukkit.getScheduler().runTask(SentienceEntity.getInstance(), () -> {
                                        PlayerClickNPCEvent event = new PlayerClickNPCEvent(player, npcName, clickType);
                                        Bukkit.getPluginManager().callEvent(event);
                                    });
                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }
                            }
                        }
                    });
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void uninjectPlayer(Player player) {
        try {
            Channel channel = this.getPlayerChannel(player);
            String inboundHandlerName = "packet_interceptor_in_" + player.getName();

            if (channel.pipeline().get(inboundHandlerName) != null) {
                channel.pipeline().remove(inboundHandlerName);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private int getEntityId(ServerboundInteractPacket packet) {
        try {
            return (int) this.entityIdField.get(packet);
        } catch (Exception exception) {
            exception.printStackTrace();
            return -1;
        }
    }

    private String getActionType(ServerboundInteractPacket packet) {
        try {
            Object action = this.actionField.get(packet);

            if (action != null && this.getTypeMethod != null) {
                Object actionType = this.getTypeMethod.invoke(action);

                if (actionType instanceof Enum<?>) {
                    return ((Enum<?>) actionType).name();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return "UNKNOWN";
    }

    private InteractionHand getInteractionHand(ServerboundInteractPacket packet) {
        try {
            Object action = this.actionField.get(packet);

            if (action != null) {
                String className = action.getClass().getSimpleName();

                if (className.isEmpty() || className.equalsIgnoreCase("1")) {
                    return InteractionHand.MAIN_HAND;
                }

                Field handField = action.getClass().getDeclaredField("hand");
                handField.setAccessible(true);
                return (InteractionHand) handField.get(action);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private Channel getPlayerChannel(Player player) throws Exception {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();

        Field connectionField = serverPlayer.connection.getClass().getSuperclass().getDeclaredField("connection");
        connectionField.setAccessible(true);

        Connection connection = (Connection) connectionField.get(serverPlayer.connection);
        Field channelField = connection.getClass().getDeclaredField("channel");
        channelField.setAccessible(true);

        return (Channel) channelField.get(connection);
    }
}
