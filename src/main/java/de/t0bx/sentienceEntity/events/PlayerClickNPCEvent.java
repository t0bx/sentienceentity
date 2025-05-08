package de.t0bx.sentienceEntity.events;

import de.t0bx.sentienceEntity.utils.ClickType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerClickNPCEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String npcName;
    private final ClickType clickType;

    public PlayerClickNPCEvent(@NotNull Player who, String npcName, ClickType clickType) {
        super(who);
        this.npcName = npcName;
        this.clickType = clickType;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
