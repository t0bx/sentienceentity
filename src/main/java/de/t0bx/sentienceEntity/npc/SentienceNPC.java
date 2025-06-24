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

package de.t0bx.sentienceEntity.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R3.CraftServer;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

@Getter
public class SentienceNPC {

    private final int entityId;

    private final GameProfile profile;

    @Setter
    private Location location;

    @Setter
    private boolean shouldLookAtPlayer;

    @Setter
    private boolean shouldSneakWithPlayer;

    private final Set<ServerPlayer> channels = new HashSet<>();

    public SentienceNPC(int entityId, GameProfile profile) {
        this.entityId = entityId;
        this.profile = profile;
    }

    public void spawn(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerLevel nmsWorld = ((CraftWorld) player.getWorld()).getHandle();

        if (this.hasSpawned(serverPlayer)) return;
        if (this.getLocation() == null) return;

        if (!this.getLocation().getWorld().getName().equalsIgnoreCase(player.getWorld().getName())) return;

        ServerPlayer fakePlayer = this.getFakePlayer();

        var actions = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER);
        try {
            Constructor<ClientboundPlayerInfoUpdatePacket> packetConstructor =
                    ClientboundPlayerInfoUpdatePacket.class.getDeclaredConstructor(RegistryFriendlyByteBuf.class);
            packetConstructor.setAccessible(true);

            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), ((CraftServer) Bukkit.getServer()).getServer().registryAccess());
            buf.writeEnumSet(actions, ClientboundPlayerInfoUpdatePacket.Action.class);
            buf.writeCollection(Collections.singletonList(this.getPlayerInfo()), (buffer, entry) -> {
                buffer.writeUUID(entry.profileId());

                for (ClientboundPlayerInfoUpdatePacket.Action action : actions) {
                    try {
                        Field writerField = action.getClass().getDeclaredField("j");
                        writerField.setAccessible(true);

                        Object writer = writerField.get(action);
                        ClientboundPlayerInfoUpdatePacket.Action.Writer writeFunc = (ClientboundPlayerInfoUpdatePacket.Action.Writer) writer;
                        writeFunc.write((RegistryFriendlyByteBuf) buffer, entry);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            var infoUpdatePacket = packetConstructor.newInstance(buf);
            serverPlayer.connection.send(infoUpdatePacket);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        var addEntityPacket = new ClientboundAddEntityPacket(
                entityId,
                fakePlayer.getUUID(),
                this.location.getX(),
                this.location.getY(),
                this.location.getZ(),
                this.location.getPitch(),
                this.location.getYaw(),
                EntityType.PLAYER,
                0,
                new Vec3(0, 0, 0),
                this.location.getYaw()
        );

        serverPlayer.connection.send(addEntityPacket);

        var metadata = new ClientboundSetEntityDataPacket(
                entityId,
                List.of(new SynchedEntityData.DataValue<>(17, EntityDataSerializers.BYTE, (byte) 127))
        );
        serverPlayer.connection.send(metadata);

        Scoreboard scoreboard = nmsWorld.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam("hidden_" + fakePlayer.getId());
        if (team == null) {
            team = scoreboard.addPlayerTeam("hidden_" + fakePlayer.getId());
        }
        team.setNameTagVisibility(Team.Visibility.NEVER);
        scoreboard.addPlayerToTeam(fakePlayer.getScoreboardName(), team);

        var teamPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
        serverPlayer.connection.send(teamPacket);

        this.channels.add(serverPlayer);
    }

    public void updateRotation(float yaw, float pitch) {
        this.getLocation().setYaw(yaw);
        this.getLocation().setPitch(pitch);

        byte yawByte = this.toRotationByte(yaw);
        byte pitchByte = this.toRotationByte(pitch);

        var rotationPacket = new ClientboundMoveEntityPacket.Rot(
                entityId,
                yawByte,
                pitchByte,
                true
        );
        try {
            Constructor<ClientboundRotateHeadPacket> headPacketConstructor =
                    ClientboundRotateHeadPacket.class.getDeclaredConstructor(FriendlyByteBuf.class);
            headPacketConstructor.setAccessible(true);

            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeVarInt(entityId);
            buf.writeByte(yawByte);

            var entityHead = headPacketConstructor.newInstance(buf);
            for (ServerPlayer player : this.channels) {
                player.connection.send(rotationPacket);
                player.connection.send(entityHead);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void updateSneaking(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        if (!this.hasSpawned(serverPlayer)) return;

        boolean playerSneaking = player.isSneaking();
        SynchedEntityData.DataValue<?> dataValue;
        if (!playerSneaking) {
            dataValue = new SynchedEntityData.DataValue<>(6, EntityDataSerializers.POSE, Pose.CROUCHING);
        } else {
            dataValue = new SynchedEntityData.DataValue<>(6, EntityDataSerializers.POSE, Pose.STANDING);
        }

        var metadataPacket = new ClientboundSetEntityDataPacket(this.getEntityId(), Collections.singletonList(dataValue));
        serverPlayer.connection.send(metadataPacket);
    }

    public void updateLookingAtPlayer(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        if (!this.hasSpawned(serverPlayer)) return;

        Location npcLocation = this.getLocation().clone().add(0, 1.62, 0);
        Location playerEyeLocation = player.getEyeLocation();

        double dx = playerEyeLocation.getX() - npcLocation.getX();
        double dy = playerEyeLocation.getY() - npcLocation.getY();
        double dz = playerEyeLocation.getZ() - npcLocation.getZ();

        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        if (distanceXZ == 0) distanceXZ = 0.001;

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distanceXZ));

        yaw = this.normalizeYaw(yaw);

        byte yawByte = this.toRotationByte(yaw);
        byte pitchByte = this.toRotationByte(pitch);

        var entityRotation = new ClientboundMoveEntityPacket.Rot(
                entityId,
                yawByte,
                pitchByte,
                true
        );
        serverPlayer.connection.send(entityRotation);

        try {
            Constructor<ClientboundRotateHeadPacket> headPacketConstructor =
                    ClientboundRotateHeadPacket.class.getDeclaredConstructor(FriendlyByteBuf.class);
            headPacketConstructor.setAccessible(true);

            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeVarInt(entityId);
            buf.writeByte(yawByte);

            var entityHead = headPacketConstructor.newInstance(buf);
            serverPlayer.connection.send(entityHead);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void despawn(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        if (!this.hasSpawned(serverPlayer)) return;

        var removeEntitiesPacket = new ClientboundRemoveEntitiesPacket(this.getEntityId());
        serverPlayer.connection.send(removeEntitiesPacket);
        this.channels.remove(serverPlayer);
    }

    public void despawnAll() {
        var removeEntitiesPacket = new ClientboundRemoveEntitiesPacket(this.getEntityId());
        for (ServerPlayer player : this.channels) {
            player.connection.send(removeEntitiesPacket);
        }
        this.channels.clear();
    }

    public void teleport(Location location) {
        this.setLocation(location);
        var entityTeleportPacket = new ClientboundTeleportEntityPacket(this.getEntityId(),
                new PositionMoveRotation(
                        new Vec3(
                        location.getX(),
                        location.getY(),
                        location.getZ()),
                        new Vec3(0, 0, 0),
                        location.getYaw(),
                        location.getPitch()
                ),
                Collections.emptySet(),
                true);
        for (ServerPlayer player : this.channels) {
            player.connection.send(entityTeleportPacket);
        }
    }

    public void changeSkin(String skinValue, String skinSignature) {
        var removePacket = new ClientboundPlayerInfoRemovePacket(Collections.singletonList(this.getProfile().getId()));
        for (ServerPlayer player : this.channels) {
            player.connection.send(removePacket);
        }

        var destroyEntityPacket = new ClientboundRemoveEntitiesPacket(this.getEntityId());
        for (ServerPlayer player : this.channels) {
            player.connection.send(destroyEntityPacket);
        }

        this.getProfile().getProperties().clear();
        this.getProfile().getProperties().put("textures", new Property("textures", skinValue, skinSignature));

        ServerPlayer fakePlayer = this.getFakePlayer();

        var actions = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER);
        try {
            Constructor<ClientboundPlayerInfoUpdatePacket> packetConstructor =
                    ClientboundPlayerInfoUpdatePacket.class.getDeclaredConstructor(RegistryFriendlyByteBuf.class);
            packetConstructor.setAccessible(true);

            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), ((CraftServer) Bukkit.getServer()).getServer().registryAccess());
            buf.writeEnumSet(actions, ClientboundPlayerInfoUpdatePacket.Action.class);
            buf.writeCollection(Collections.singletonList(this.getPlayerInfo()), (buffer, entry) -> {
                buffer.writeUUID(entry.profileId());

                for (ClientboundPlayerInfoUpdatePacket.Action action : actions) {
                    try {
                        Field writerField = action.getClass().getDeclaredField("j");
                        writerField.setAccessible(true);

                        Object writer = writerField.get(action);
                        ClientboundPlayerInfoUpdatePacket.Action.Writer writeFunc = (ClientboundPlayerInfoUpdatePacket.Action.Writer) writer;
                        writeFunc.write((RegistryFriendlyByteBuf) buffer, entry);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            var infoUpdatePacket = packetConstructor.newInstance(buf);
            for (ServerPlayer player : this.channels) {
                player.connection.send(infoUpdatePacket);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        var addEntityPacket = new ClientboundAddEntityPacket(
                entityId,
                fakePlayer.getUUID(),
                this.location.getX(),
                this.location.getY(),
                this.location.getZ(),
                this.location.getPitch(),
                this.location.getYaw(),
                EntityType.PLAYER,
                0,
                new Vec3(0, 0, 0),
                this.location.getYaw()
        );

        for (ServerPlayer player : this.channels) {
            player.connection.send(addEntityPacket);
        }

        var metadata = new ClientboundSetEntityDataPacket(
                entityId,
                List.of(new SynchedEntityData.DataValue<>(17, EntityDataSerializers.BYTE, (byte) 127))
        );
        for (ServerPlayer player : this.channels) {
            player.connection.send(metadata);
        }

        ServerLevel nmsWorld = ((CraftWorld) this.getLocation().getWorld()).getHandle();
        Scoreboard scoreboard = nmsWorld.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam("hidden_" + fakePlayer.getId());
        if (team == null) {
            team = scoreboard.addPlayerTeam("hidden_" + fakePlayer.getId());
        }
        team.setNameTagVisibility(Team.Visibility.NEVER);
        scoreboard.addPlayerToTeam(fakePlayer.getScoreboardName(), team);

        var teamPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
        for (ServerPlayer player : this.channels) {
            player.connection.send(teamPacket);
        }
    }

    public boolean hasSpawned(ServerPlayer serverPlayer) {
        return this.channels.contains(serverPlayer);
    }

    private ServerPlayer getFakePlayer() {
        ServerLevel nmsWorld = ((CraftWorld) this.getLocation().getWorld()).getHandle();
        return new ServerPlayer(
                ((CraftServer) Bukkit.getServer()).getServer(),
                nmsWorld,
                profile,
                ClientInformation.createDefault()
        );
    }

    private ClientboundPlayerInfoUpdatePacket.Entry getPlayerInfo() {
        return new ClientboundPlayerInfoUpdatePacket.Entry(
                this.profile.getId(),
                this.profile,
                false,
                0,
                GameType.DEFAULT_MODE,
                null,
                true,
                0,
                null
        );
    }

    private float normalizeYaw(float yaw) {
        yaw %= 360;
        if (yaw < 0) yaw += 360;
        return yaw;
    }

    private byte toRotationByte(float degrees) {
        degrees = degrees % 360;
        if (degrees < 0) degrees += 360;

        return (byte) (degrees * 256.0F / 360.0F);
    }
}