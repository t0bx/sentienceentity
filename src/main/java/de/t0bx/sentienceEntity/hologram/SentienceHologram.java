package de.t0bx.sentienceEntity.hologram;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class SentienceHologram {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final double LINE_HEIGHT = 0.25;

    private final int entityId;
    private final UUID uuid;

    private final Location baseLocation;
    private final Map<Integer, HologramLine> hologramLines;

    @Setter
    private Location location;

    private final Set<Object> channels = new HashSet<>();

    public SentienceHologram(int entityId, UUID uuid, Location baseLocation) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.baseLocation = baseLocation;

        this.hologramLines = new HashMap<>();
        this.location = baseLocation.clone();
    }

    public void addLine(String line) {
        int lineIndex = hologramLines.size();
        int lineEntityId = SpigotReflectionUtil.generateEntityId();
        Location lineLocation = this.baseLocation.clone();
        lineLocation.setPosition(new Vector3d(
                this.baseLocation.getX(),
                this.baseLocation.getY() + 1.8 - (LINE_HEIGHT * (lineIndex + 1)),
                this.baseLocation.getZ()
        ));

        HologramLine hologramLine = new HologramLine(lineEntityId, this.uuid, line, lineLocation);
        hologramLines.put(lineIndex, hologramLine);

        spawnLine(hologramLine);
        moveExistingLinesUp();
    }

    private void spawnLine(HologramLine line) {
        Location location = line.getLocation();
        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                line.getEntityId(),
                UUID.randomUUID(),
                EntityTypes.ARMOR_STAND,
                location,
                location.getYaw(),
                0,
                null
        );

        List<EntityData> data = List.of(
                new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20),
                new EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(miniMessage.deserialize(line.getText()))),
                new EntityData(3, EntityDataTypes.BOOLEAN, true),
                new EntityData(5, EntityDataTypes.BOOLEAN, true),
                new EntityData(15, EntityDataTypes.BYTE, (byte) 0x19)
        );

        WrapperPlayServerEntityMetadata metaPacket = new WrapperPlayServerEntityMetadata(line.getEntityId(), data);

        for (Object channel : channels) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, spawnPacket);
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, metaPacket);
        }
    }

    public void removeLine(int index) {
        if (!hologramLines.containsKey(index)) return;

        HologramLine line = hologramLines.remove(index);
        if (line == null) return;

        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(line.getEntityId());
        for (Object channel : channels) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, destroyPacket);
        }

        updateLinesAfterRemoval();
    }

    private void updateLinesAfterRemoval() {
        List<Map.Entry<Integer, HologramLine>> linesList = new ArrayList<>(hologramLines.entrySet());

        for (int newIndex = 0; newIndex < linesList.size(); newIndex++) {
            Map.Entry<Integer, HologramLine> entry = linesList.get(newIndex);
            HologramLine line = entry.getValue();

            Location newLocation = baseLocation.clone();
            newLocation.setPosition(new Vector3d(
                    baseLocation.getX(),
                    baseLocation.getY() + 1.8 + (LINE_HEIGHT * (linesList.size() - newIndex - 1)),
                    baseLocation.getZ()
            ));

            line.setLocation(newLocation);

            WrapperPlayServerEntityTeleport teleport = new WrapperPlayServerEntityTeleport(
                    line.getEntityId(),
                    newLocation,
                    true
            );

            for (Object channel : channels) {
                PacketEvents.getAPI().getProtocolManager().sendPacket(channel, teleport);
            }

            hologramLines.put(newIndex, line);
        }
    }

    private void moveExistingLinesUp() {
        List<HologramLine> linesList = new ArrayList<>(hologramLines.values());
        for (int i = 0; i < linesList.size(); i++) {
            HologramLine line = linesList.get(i);

            Location newLocation = baseLocation.clone();
            newLocation.setPosition(new Vector3d(
                    baseLocation.getX(),
                    baseLocation.getY() + 1.8 + (LINE_HEIGHT * (linesList.size() - i - 1)),
                    baseLocation.getZ()
            ));

            line.setLocation(newLocation);

            WrapperPlayServerEntityTeleport teleport = new WrapperPlayServerEntityTeleport(
                    line.getEntityId(),
                    newLocation,
                    true
            );

            for (Object channel : channels) {
                PacketEvents.getAPI().getProtocolManager().sendPacket(channel, teleport);
            }
        }
    }

    public void updateLine(int index, String newText) {
        if (!hologramLines.containsKey(index)) return;

        HologramLine line = hologramLines.get(index);
        if (line == null) return;

        line.setText(newText);
        List<EntityData> data = List.of(
                new EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(miniMessage.deserialize(newText)))
        );

        WrapperPlayServerEntityMetadata metaPacket = new WrapperPlayServerEntityMetadata(line.getEntityId(), data);
        for (Object channel : channels) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, metaPacket);
        }
    }

    public void spawn(Player player) {
        if (hasSpawned(player)) return;
        Object channel = PacketEvents.getAPI().getPlayerManager().getChannel(player);
        if (channel == null) return;

        channels.add(channel);

        for (HologramLine line : hologramLines.values()) {
            spawnLine(line);
        }
    }

    public void destroy() {
        for (HologramLine line : hologramLines.values()) {
            WrapperPlayServerDestroyEntities destroy = new WrapperPlayServerDestroyEntities(line.getEntityId());
            for (Object channel : channels) {
                PacketEvents.getAPI().getProtocolManager().sendPacket(channel, destroy);
            }
        }
        channels.clear();
        hologramLines.clear();
    }

    public boolean hasSpawned(Player player) {
        Object channel = PacketEvents.getAPI().getPlayerManager().getChannel(player);
        if (channel == null) return false;
        return channels.contains(channel);
    }
}