package de.t0bx.sentienceEntity.hologram;

import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Hologram {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final double LINE_HEIGHT = 0.25;

    private final Location baseLocation;
    private final List<ArmorStand> lines;
    private final List<String> textLines;

    @Getter
    private final UUID hologramId;

    public Hologram(Location location) {
        this.baseLocation = location.clone().add(0, 1.8, 0);
        this.lines = new ArrayList<>();
        this.textLines = new ArrayList<>();
        this.hologramId = UUID.randomUUID();
    }

    public Hologram addLine(String text) {
        textLines.add(text);

        moveExistingLinesUp();

        Location lineLocation = baseLocation.clone();
        ArmorStand armorStand = createArmorStand(lineLocation);

        armorStand.customName(miniMessage.deserialize(text));

        lines.add(armorStand);

        return this;
    }

    private void moveExistingLinesUp() {
        for (ArmorStand armorStand : lines) {
            Location currentLoc = armorStand.getLocation();
            armorStand.teleport(currentLoc.add(0, LINE_HEIGHT, 0));
        }
    }

    private ArmorStand createArmorStand(Location location) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setCustomNameVisible(true);
        armorStand.setMarker(true);
        armorStand.setSilent(true);
        armorStand.setPersistent(false);

        return armorStand;
    }

    public Hologram setLine(int index, String text) {
        if (index >= 0 && index < lines.size()) {
            ArmorStand armorStand = lines.get(index);
            armorStand.customName(miniMessage.deserialize(text));
            textLines.set(index, text);
        }
        return this;
    }

    public Hologram removeLine(int index) {
        if (index >= 0 && index < lines.size()) {
            ArmorStand removed = lines.remove(index);
            removed.remove();

            textLines.remove(index);

            for (int i = index; i < lines.size(); i++) {
                ArmorStand stand = lines.get(i);
                Location loc = stand.getLocation();
                stand.teleport(loc.subtract(0, LINE_HEIGHT, 0));
            }
        }
        return this;
    }

    public void destroy() {
        for (ArmorStand armorStand : lines) {
            armorStand.remove();
        }
        lines.clear();
        textLines.clear();
    }

    public int getLineCount() {
        return lines.size();
    }

    public List<String> getTextLines() {
        return new ArrayList<>(textLines);
    }

    public void teleport(Location newBaseLocation) {
        Location adjustedLocation = newBaseLocation.clone().add(0, 1.6, 0);

        double xOffset = adjustedLocation.getX() - baseLocation.getX();
        double yOffset = adjustedLocation.getY() - baseLocation.getY();
        double zOffset = adjustedLocation.getZ() - baseLocation.getZ();

        this.baseLocation.setX(adjustedLocation.getX());
        this.baseLocation.setY(adjustedLocation.getY());
        this.baseLocation.setZ(adjustedLocation.getZ());

        for (ArmorStand stand : lines) {
            Location currentLoc = stand.getLocation();

            Location newLoc = currentLoc.clone().add(xOffset, yOffset, zOffset);
            stand.teleport(newLoc);
        }
    }
}
