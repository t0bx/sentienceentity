package de.t0bx.sentienceEntity.network.version.registries;

import de.t0bx.sentienceEntity.network.utils.EntityType;
import de.t0bx.sentienceEntity.network.version.ProtocolVersion;
import de.t0bx.sentienceEntity.network.version.VersionRegistry;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class EntityTypeRegistry {
    private static final Map<ProtocolVersion, EnumMap<EntityType, Integer>> REGISTRY = new HashMap<>();

    static {
        var v1214 = new EnumMap<EntityType, Integer>(EntityType.class);
        v1214.put(EntityType.ARMOR_STAND, 5);
        v1214.put(EntityType.PLAYER, 147);
        REGISTRY.put(ProtocolVersion.V1_21_4, v1214);

        var v1215 = new EnumMap<EntityType, Integer>(EntityType.class);
        v1215.put(EntityType.ARMOR_STAND, 5);
        v1215.put(EntityType.PLAYER, 148);
        REGISTRY.put(ProtocolVersion.V1_21_5, v1215);
    }

    public static int getEntityType(EntityType entityType) {
        ProtocolVersion version = VersionRegistry.getVersion();
        EnumMap<EntityType, Integer> map = REGISTRY.get(version);
        if (map == null || !map.containsKey(entityType)) {
            throw new IllegalArgumentException("No packet mapping for entity Type " + entityType + " in version " + version);
        }
        return map.get(entityType);
    }
}
