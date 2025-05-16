package de.t0bx.sentienceEntity.packetlistener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.events.PlayerClickNPCEvent;
import de.t0bx.sentienceEntity.npc.NPCsHandler;
import de.t0bx.sentienceEntity.utils.ClickType;
import org.bukkit.Bukkit;

public class PacketReceiveListener implements PacketListener {

    private final NPCsHandler npcsHandler;

    public PacketReceiveListener(NPCsHandler npcsHandler) {
        this.npcsHandler = npcsHandler;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;

        WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
        int entityId = packet.getEntityId();
        if (!this.npcsHandler.getNpcIds().contains(entityId)) return;

        if (packet.getHand() != InteractionHand.MAIN_HAND) return;

        WrapperPlayClientInteractEntity.InteractAction action = packet.getAction();

        ClickType clickType;
        if (action == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            clickType = ClickType.LEFT_CLICK;
        } else if (action == WrapperPlayClientInteractEntity.InteractAction.INTERACT) {
            clickType = ClickType.RIGHT_CLICK;
        } else {
            return;
        }

        String npcName = this.npcsHandler.getNpcNameFromId(entityId);
        if (npcName == null) return;

        Bukkit.getScheduler().runTask(SentienceEntity.getInstance(), () -> {
           PlayerClickNPCEvent playerClickNPCEvent = new PlayerClickNPCEvent(event.getPlayer(), npcName, clickType);
           Bukkit.getPluginManager().callEvent(playerClickNPCEvent);
        });
    }
}
