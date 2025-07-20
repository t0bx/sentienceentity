package de.t0bx.sentienceEntity.path.serializer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.Serial;
import java.io.Serializable;

public class SerializableLocation implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public String world;
    public double x, y, z;
    public float yaw, pitch;

    /**
     * Constructs a SerializableLocation instance from a given {@link Location} object.
     * This constructor extracts and stores the world name, coordinates (x, y, z),
     * and orientation (yaw, pitch) from the provided {@link Location}.
     *
     * @param location The {@link Location} object from which to extract the world name,
     *                 coordinates, and orientation data.
     */
    public SerializableLocation(Location location) {
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    /**
     * Converts the current {@link SerializableLocation} into a corresponding {@link Location} object.
     * This method utilizes the stored world name, coordinates, and orientation data to create
     * a new {@link Location} instance. If the world specified by the stored name does not exist,
     * the method returns null.
     *
     * @return A {@link Location} object representing the stored data, or null if the world
     *         specified by the stored name cannot be found.
     */
    public Location toBukkitLocation() {
        World bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null) return null;
        return new Location(bukkitWorld, x, y, z, yaw, pitch);
    }
}
