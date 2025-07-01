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

package de.t0bx.sentienceEntity.events;

import de.t0bx.sentienceEntity.utils.ClickType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerClickNpcEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String npcName;
    private final ClickType clickType;

    public PlayerClickNpcEvent(@NotNull Player who, String npcName, ClickType clickType) {
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
