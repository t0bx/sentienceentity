package de.t0bx.sentienceEntity.hologram;

import com.github.retrooper.packetevents.protocol.world.Location;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class HologramLine {
    private final int entityId;
    private final UUID hologramUUID;

    @Setter
    private String text;
    @Setter
    private Location location;

    public HologramLine(int entityId, UUID hologramUUID, String text, Location location) {
        this.entityId = entityId;
        this.hologramUUID = hologramUUID;
        this.text = text;
        this.location = location;
    }
}
