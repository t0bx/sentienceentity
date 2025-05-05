package de.t0bx.sentienceEntity.npc;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import de.t0bx.sentienceEntity.utils.SentienceLocation;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
public class SentienceNPC {

    private final int entityId;
    private final UserProfile profile;

    @Setter
    private SentienceLocation location;

    @Setter
    private boolean shouldLookAtPlayer;

    @Setter
    private boolean shouldSneakWithPlayer;

    private final Set<Object> channels = new HashSet<>();

    public SentienceNPC(int entityId, UserProfile profile) {
        this.entityId = entityId;
        this.profile = profile;
    }

    public void spawn(Player player) {
        if (this.hasSpawned(player)) return;
        if (this.getLocation() == null) return;

        if (!player.getWorld().getName().equals(this.getLocation().getWorld().getName())) return;

        Object channel = PacketEvents.getAPI().getPlayerManager().getChannel(player);
        if (channel == null) return;

        this.channels.add(channel);
        WrapperPlayServerPlayerInfoUpdate playerInfoPacket = new WrapperPlayServerPlayerInfoUpdate(
                WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                this.getPlayerInfo()
        );
        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, playerInfoPacket);

        WrapperPlayServerSpawnEntity spawnEntityPacket = new WrapperPlayServerSpawnEntity(
                this.getEntityId(),
                this.getProfile().getUUID(),
                EntityTypes.PLAYER,
                this.getLocation(),
                this.getLocation().getYaw(),
                0,
                null
        );
        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, spawnEntityPacket);

        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(
                this.getEntityId(),
                Collections.singletonList(new EntityData(17, EntityDataTypes.BYTE, (byte) 127))
        );
        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, metadataPacket);

        WrapperPlayServerTeams teamsPacket = new WrapperPlayServerTeams(profile.getName(),
                WrapperPlayServerTeams.TeamMode.CREATE,
                Optional.of(
                        new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                                Component.text(profile.getName()),
                                null,
                                null,
                                WrapperPlayServerTeams.NameTagVisibility.NEVER,
                                WrapperPlayServerTeams.CollisionRule.ALWAYS,
                                null,
                                WrapperPlayServerTeams.OptionData.NONE
                        )),
                getProfile().getName()
        );

        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, teamsPacket);
    }

    public void updateRotation(float yaw, float pitch) {
        this.getLocation().setYaw(yaw);
        this.getLocation().setPitch(pitch);
        WrapperPlayServerEntityRotation entityRotation =
                new WrapperPlayServerEntityRotation(getEntityId(), yaw, pitch, true);

        WrapperPlayServerEntityHeadLook headYaw =
                new WrapperPlayServerEntityHeadLook(getEntityId(), yaw);
        for (Object channel : channels) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, entityRotation);
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, headYaw);
        }
    }

    public void updateSneaking(Player player) {
        if (!this.hasSpawned(player)) return;
        Object channel = PacketEvents.getAPI().getPlayerManager().getChannel(player);
        if (channel == null) return;

        boolean playerSneaking = player.isSneaking();
        EntityData data;
        if (!playerSneaking) {
            data = new EntityData(6, EntityDataTypes.ENTITY_POSE, EntityPose.CROUCHING);
        } else {
            data = new EntityData(6, EntityDataTypes.ENTITY_POSE, EntityPose.STANDING);
        }

        WrapperPlayServerEntityMetadata metaDataPacket = new WrapperPlayServerEntityMetadata(this.getEntityId(), Collections.singletonList(data));

        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, metaDataPacket);
    }

    public void updateLookingAtPlayer(Player player) {
        if (!this.hasSpawned(player)) return;
        Object channel = PacketEvents.getAPI().getPlayerManager().getChannel(player);
        if (channel == null) return;

        org.bukkit.Location npcLoc = SpigotConversionUtil.toBukkitLocation(player.getLocation().getWorld(), this.getLocation()).add(0, 1.62, 0);
        org.bukkit.Location playerEyeLoc = player.getEyeLocation();

        double dx = playerEyeLoc.getX() - npcLoc.getX();
        double dy = playerEyeLoc.getY() - npcLoc.getY();
        double dz = playerEyeLoc.getZ() - npcLoc.getZ();

        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        if (distanceXZ == 0) distanceXZ = 0.001;

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distanceXZ));

        yaw = normalizeYaw(yaw);

        WrapperPlayServerEntityRotation entityRotationPacket = new WrapperPlayServerEntityRotation(
                this.getEntityId(),
                yaw,
                pitch,
                true
        );

        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, entityRotationPacket);

        WrapperPlayServerEntityHeadLook headLookPacket = new WrapperPlayServerEntityHeadLook(this.getEntityId(), yaw);

        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, headLookPacket);
    }

    public void despawn(Player player) {
        if (!this.hasSpawned(player)) return;

        Object channel = PacketEvents.getAPI().getPlayerManager().getChannel(player);
        if (channel != null) {
            WrapperPlayServerDestroyEntities destroyEntitiesPacket = new WrapperPlayServerDestroyEntities(this.getEntityId());
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, destroyEntitiesPacket);
            this.channels.remove(channel);
        }
    }

    public void despawnAll() {
        WrapperPlayServerDestroyEntities destroyEntitiesPacket = new WrapperPlayServerDestroyEntities(this.getEntityId());
        for (Object channel : this.channels) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, destroyEntitiesPacket);
        }
        this.channels.clear();
    }

    public void teleport(SentienceLocation location) {
        this.setLocation(location);
        WrapperPlayServerEntityTeleport entityTeleportPacket = new WrapperPlayServerEntityTeleport(this.getEntityId(), location, true);
        for (Object channels : this.channels) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channels, entityTeleportPacket);
        }
    }

    public void changeSkin(String skinValue, String skinSignature) {
        WrapperPlayServerPlayerInfoRemove removePacket = new WrapperPlayServerPlayerInfoRemove(this.getProfile().getUUID());
        for (Object channel : channels) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, removePacket);
        }

        WrapperPlayServerDestroyEntities destroyEntitiesPacket = new WrapperPlayServerDestroyEntities(this.getEntityId());
        for (Object channel : this.channels) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, destroyEntitiesPacket);
        }

        this.getProfile().setTextureProperties(Collections.singletonList(new TextureProperty("textures", skinValue, skinSignature)));
        WrapperPlayServerPlayerInfoUpdate playerInfoPacket = new WrapperPlayServerPlayerInfoUpdate(
                WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                this.getPlayerInfo()
        );
        for (Object channel : this.channels) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, playerInfoPacket);
        }

        WrapperPlayServerSpawnEntity spawnEntityPacket = new WrapperPlayServerSpawnEntity(
                this.getEntityId(),
                this.getProfile().getUUID(),
                EntityTypes.PLAYER,
                this.getLocation(),
                this.getLocation().getYaw(),
                0,
                null
        );
        for (Object channel : this.channels) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, spawnEntityPacket);
        }

        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(
                this.getEntityId(),
                Collections.singletonList(new EntityData(17, EntityDataTypes.BYTE, (byte) 127))
        );
        for (Object channel : this.channels) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, metadataPacket);
        }

        WrapperPlayServerTeams teamsPacket = new WrapperPlayServerTeams(profile.getName(),
                WrapperPlayServerTeams.TeamMode.CREATE,
                Optional.of(
                        new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                                Component.text(profile.getName()),
                                null,
                                null,
                                WrapperPlayServerTeams.NameTagVisibility.NEVER,
                                WrapperPlayServerTeams.CollisionRule.ALWAYS,
                                null,
                                WrapperPlayServerTeams.OptionData.NONE
                        )),
                getProfile().getName()
        );
        for (Object channel : this.channels) {
            PacketEvents.getAPI().getProtocolManager().sendPacket(channel, teamsPacket);
        }
    }

    public boolean hasSpawned(Player player) {
        Object channel = PacketEvents.getAPI().getPlayerManager().getChannel(player);
        return channel != null && channels.contains(channel);
    }

    public WrapperPlayServerPlayerInfoUpdate.PlayerInfo getPlayerInfo() {
        return new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                this.getProfile(),
                false,
                0,
                GameMode.SURVIVAL,
                null,
                null
        );
    }

    private float normalizeYaw(float yaw) {
        yaw %= 360;
        if (yaw < 0) yaw += 360;
        return yaw;
    }
}