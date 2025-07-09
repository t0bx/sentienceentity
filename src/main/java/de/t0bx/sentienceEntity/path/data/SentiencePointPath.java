package de.t0bx.sentienceEntity.path.data;

import lombok.Data;
import org.bukkit.Location;

@Data
public class SentiencePointPath {
    private Location location;
    private boolean isTeleport;
}
