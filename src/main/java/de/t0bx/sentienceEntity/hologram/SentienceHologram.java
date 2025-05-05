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
    private final List<Integer> lines;
    private final List<String> textLines;

    @Setter
    private Location location;

    private final Set<Object> channels = new HashSet<>();

    public SentienceHologram(int entityId, UUID uuid, Location baseLocation) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.baseLocation = baseLocation;

        this.lines = new ArrayList<>();
        this.textLines = new ArrayList<>();
    }

    public void addLine(String line) {
        this.textLines.add(line);

        this.moveExistingLinesUp();


    }

    private void moveExistingLinesUp() {
        for (int entityId : lines) {
            Location newLocation = this.baseLocation.clone();
            newLocation.setPosition(new Vector3d(this.baseLocation.getX(), this.baseLocation.getY() + this.LINE_HEIGHT, this.baseLocation.getZ()));
            WrapperPlayServerEntityTeleport teleport = new WrapperPlayServerEntityTeleport(
                    entityId,
                    newLocation,
                    true
            );

            for (Object channel : channels) {
                PacketEvents.getAPI().getProtocolManager().sendPacket(teleport, channel);
            }
        }
    }

    public void spawn(Player player) {
        if (hasSpawned(player)) return;
        Object channel = PacketEvents.getAPI().getPlayerManager().getChannel(player);
        if (channel == null) return;

        WrapperPlayServerSpawnEntity spawnEntityPacket = new WrapperPlayServerSpawnEntity(
                this.getEntityId(),
                this.getUuid(),
                EntityTypes.ARMOR_STAND,
                this.getLocation(),
                this.getLocation().getYaw(),
                0,
                null
        );

        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, spawnEntityPacket);

        List<EntityData> entityDataList = List.of(
                new EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, MiniMessage.miniMessage().deserialize("<green>nigga")),
                new EntityData(3, EntityDataTypes.BOOLEAN, true),
                new EntityData(5, EntityDataTypes.BOOLEAN, true),
                new EntityData(15, EntityDataTypes.BYTE, (byte) 0x19)
        );

        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(
                this.getEntityId(),
                entityDataList
        );

        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, metadataPacket);
    }

    public void destroy() {
        WrapperPlayServerDestroyEntities destroyEntitiesPacket = new WrapperPlayServerDestroyEntities(
                this.getEntityId()
        );
        for (Object channel : channels) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(destroyEntitiesPacket, channel);
        }
    }

    public boolean hasSpawned(Player player) {
        Object channel = PacketEvents.getAPI().getPlayerManager().getChannel(player);
        if (channel == null) return false;
        return channels.contains(channel);
    }
}
