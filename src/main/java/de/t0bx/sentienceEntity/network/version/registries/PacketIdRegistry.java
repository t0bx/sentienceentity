package de.t0bx.sentienceEntity.network.version.registries;

import de.t0bx.sentienceEntity.network.utils.PacketId;
import de.t0bx.sentienceEntity.network.version.ProtocolVersion;
import de.t0bx.sentienceEntity.network.version.VersionRegistry;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PacketIdRegistry {
    private static final Map<ProtocolVersion, EnumMap<PacketId, Integer>> REGISTRY = new HashMap<>();

    static {
        var v1214 = new EnumMap<PacketId, Integer>(PacketId.class);
        v1214.put(PacketId.PLAYER_INFO_UPDATE, 0x40);
        v1214.put(PacketId.PLAYER_INFO_REMOVE, 0x3F);
        v1214.put(PacketId.SPAWN_ENTITY, 0x01);
        v1214.put(PacketId.SET_ENTITY_METADATA, 0x5D);
        v1214.put(PacketId.SET_HEAD_ROTATION, 0x4D);
        v1214.put(PacketId.UPDATE_ENTITY_ROTATION, 0x32);
        v1214.put(PacketId.REMOVE_ENTITY, 0x47);
        v1214.put(PacketId.TELEPORT_ENTITY, 0x20);
        v1214.put(PacketId.INTERACT_ENTITY, 0x18);
        v1214.put(PacketId.SET_PLAYER_TEAM, 0x67);
        REGISTRY.put(ProtocolVersion.V1_21_4, v1214);

        var v1215 = new EnumMap<PacketId, Integer>(PacketId.class);
        v1215.put(PacketId.PLAYER_INFO_UPDATE, 0x3F);
        v1215.put(PacketId.PLAYER_INFO_REMOVE, 0x3E);
        v1215.put(PacketId.SPAWN_ENTITY, 0x01);
        v1215.put(PacketId.SET_ENTITY_METADATA, 0x5C);
        v1215.put(PacketId.SET_HEAD_ROTATION, 0x4C);
        v1215.put(PacketId.UPDATE_ENTITY_ROTATION, 0x31);
        v1215.put(PacketId.REMOVE_ENTITY, 0x46);
        v1215.put(PacketId.TELEPORT_ENTITY, 0x1F);
        v1215.put(PacketId.INTERACT_ENTITY, 0x18);
        v1215.put(PacketId.SET_PLAYER_TEAM, 0x66);
        REGISTRY.put(ProtocolVersion.V1_21_5, v1215);
    }

    public static int getPacketId(PacketId packetId) {
        ProtocolVersion version = VersionRegistry.getVersion();
        EnumMap<PacketId, Integer> map = REGISTRY.get(version);
        if (map == null || !map.containsKey(packetId)) {
            throw new IllegalArgumentException("No packet mapping for packet id " + packetId + " in version " + version);
        }
        return map.get(packetId);
    }
}
