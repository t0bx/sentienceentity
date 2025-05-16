package de.t0bx.sentienceEntity.utils;

import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;

@Getter
@Setter
public class SentienceLocation extends Location {
    private World world;
    private Vector3d position;
    private float yaw;
    private float pitch;

    public SentienceLocation(Vector3d position, float yaw, float pitch, World world) {
        super(position, yaw, pitch);
        this.world = world;
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public SentienceLocation clone() {
        return new SentienceLocation(this.position, this.yaw, this.pitch, this.world);
    }

    public static SentienceLocation fromBukkitLocation(org.bukkit.Location location) {
        return new SentienceLocation(
                new Vector3d(
                        location.getX(),
                        location.getY(),
                        location.getZ()),
                location.getYaw(),
                location.getPitch(),
                location.getWorld()
        );
    }
}
