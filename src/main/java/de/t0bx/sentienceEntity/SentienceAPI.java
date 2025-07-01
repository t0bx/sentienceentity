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

package de.t0bx.sentienceEntity;

import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.npc.NpcsHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
public class SentienceAPI {

    private final NpcsHandler npcsHandler;
    private final HologramManager hologramManager;

    @Setter
    private boolean isApiOnly;

    public SentienceAPI() {
        this.npcsHandler = SentienceEntity.getInstance().getNpcshandler();
        this.hologramManager = SentienceEntity.getInstance().getHologramManager();
        this.isApiOnly = false;
    }
}
