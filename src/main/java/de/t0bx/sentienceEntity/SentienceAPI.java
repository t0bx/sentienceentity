package de.t0bx.sentienceEntity;

import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.npc.NPCsHandler;
import lombok.Getter;

@Getter
public class SentienceAPI {

    private final NPCsHandler npcsHandler;
    private final HologramManager hologramManager;

    public SentienceAPI() {
        this.npcsHandler = SentienceEntity.getInstance().getNpcshandler();
        this.hologramManager = SentienceEntity.getInstance().getHologramManager();
    }
}
