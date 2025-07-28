package de.t0bx.sentienceEntity.listener;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.events.PlayerClickNpcEvent;
import de.t0bx.sentienceEntity.hologram.HologramLine;
import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.hologram.SentienceHologram;
import de.t0bx.sentienceEntity.network.interact.InteractHand;
import de.t0bx.sentienceEntity.network.interact.InteractType;
import de.t0bx.sentienceEntity.network.inventory.equipment.Equipment;
import de.t0bx.sentienceEntity.npc.NpcsHandler;
import de.t0bx.sentienceEntity.npc.SentienceNPC;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;

import static de.t0bx.sentienceEntity.utils.MessageUtils.sendMessage;

public class PlayerClickNpcListener implements Listener {

    private final NpcsHandler npcsHandler;
    private final HologramManager hologramManager;
    private final List<Player> inspectList;

    private final MiniMessage miniMessage;
    private final String prefix;

    public PlayerClickNpcListener(SentienceEntity sentienceEntity) {
        this.npcsHandler = sentienceEntity.getNpcshandler();
        this.hologramManager = sentienceEntity.getHologramManager();
        this.inspectList = sentienceEntity.getInspectList();

        this.miniMessage = MiniMessage.miniMessage();
        this.prefix = sentienceEntity.getPrefix();
    }

    @EventHandler
    public void onPlayerClickNpc(PlayerClickNpcEvent event) {
        Player player = event.getPlayer();

        if (!this.inspectList.contains(player)) return;

        if (event.getInteractType() == InteractType.ATTACK) {
            SentienceNPC npc = this.npcsHandler.getNPC(event.getNpcName());
            if (npc == null) return;

            sendMessage(player, this.miniMessage.deserialize(" "));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Name: <white>" + npc.getName()));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Type: <white>" + npc.getEntityType().name()));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Location: X: <white>"
                    + String.format("%.3f", npc.getLocation().getX())
                    + " <gray>Y: <white>" + String.format("%.3f", npc.getLocation().getY())
                    + " <gray>Z: <white>" + String.format("%.3f", npc.getLocation().getZ())));

            if (npc.getEquipmentData() == null) {
                sendMessage(player, this.miniMessage.deserialize(this.prefix + "Equipment: <red>None"));
            } else {
                SentienceNPC.EquipmentData data = npc.getEquipmentData();
                sendMessage(player, this.miniMessage.deserialize(this.prefix + "Equipment: (<white>" + data.getEquipment().size() + "<gray>)"));
                for (Equipment equipment : data.getEquipment()) {
                    sendMessage(player, this.miniMessage.deserialize(this.prefix + "Equipment: " +
                            "(<white>" + equipment.getSlot().name() + "<gray>) " +
                            "Item: (<white>" + equipment.getItemStack().getType().name() + "<gray>)"));
                }
            }

            sendMessage(player, this.miniMessage.deserialize(this.prefix + "ShouldSneakWithPlayers: " + (npc.isShouldSneakWithPlayer() ? "<green>✔" : "<red>✘")));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "ShouldLookAtPlayers: " + (npc.isShouldLookAtPlayer() ? "<green>✔" : "<red>✘")));
            sendMessage(player, this.miniMessage.deserialize(" "));
            return;
        }

        if (event.getInteractHand() != InteractHand.MAIN_HAND) return;

        if (event.getInteractType() == InteractType.INTERACT) {
            SentienceHologram hologram = this.hologramManager.getHologram(event.getNpcName());
            if (hologram == null) {
                sendMessage(player, this.miniMessage.deserialize(this.prefix + "No Hologram bound to npc: <white>" + event.getNpcName()));
                return;
            }

            sendMessage(player, this.miniMessage.deserialize(" "));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Hologram-bound to npc: <white>" + event.getNpcName()));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Location: X: <white>"
                    + String.format("%.3f", hologram.getLocation().getX())
                    + " <gray>Y: <white>" + String.format("%.3f", hologram.getLocation().getY())
                    + " <gray>Z: <white>" + String.format("%.3f", hologram.getLocation().getZ())));
            sendMessage(player, this.miniMessage.deserialize(this.prefix + "Amount of Lines: (<white>" + hologram.getHologramLines().size() + "<gray>)"));

            for (Map.Entry<Integer, HologramLine> entry : hologram.getHologramLines().entrySet()) {
                sendMessage(player, this.miniMessage.deserialize(this.prefix + "Line: " + entry.getKey() +
                        ": <white>" + (entry.getValue().getItemStack() != null ? entry.getValue().getItemStack().getType().name() : entry.getValue().getText())));
            }

            sendMessage(player, this.miniMessage.deserialize(" "));
        }
    }
}
