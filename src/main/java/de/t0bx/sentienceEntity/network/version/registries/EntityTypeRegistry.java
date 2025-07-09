/**
 SentienceEntity API License v1.1
 Copyright (c) 2025 (t0bx)

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to use, copy, modify, and integrate the Software into their own projects, including commercial and closed-source projects, subject to the following conditions:

 1. Attribution:
 You must give appropriate credit to the original author ("Tobias Schuster" or "t0bx"), provide a link to the source or official page if available, and indicate if changes were made. You must do so in a reasonable and visible manner, such as in your plugin.yml, README, or about page.

 2. No Redistribution or Resale:
 You may NOT sell, redistribute, or otherwise make the original Software or modified standalone versions of it available as a product (free or paid), plugin, or downloadable file, unless you have received prior written permission from the author. This includes publishing the plugin on any marketplace (e.g., SpigotMC, MC-Market, Polymart) or including it in paid bundles.

 3. Use as Dependency/API:
 You are allowed to use this Software as a dependency or library in your own plugin or project, including in paid products, as long as attribution is given and the Software itself is not being sold or published separately.

 4. No Misrepresentation:
 You may not misrepresent the origin of the Software. You must clearly distinguish your own modifications from the original work. The original author's name may not be removed from the source files or documentation.

 5. License Retention:
 This license notice and all conditions must be preserved in all copies or substantial portions of the Software.

 6. Disclaimer:
 THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY ARISING FROM THE USE OF THIS SOFTWARE.

 ---

 Summary (non-binding):
 You may use this plugin in your projects, even commercially, but you may not resell or republish it. Always give credit to t0bx.
 */

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

        var v1215 = cloneWithChanges(v1214, Map.of(
               EntityType.PLAYER, 148
        ));
        REGISTRY.put(ProtocolVersion.V1_21_5, v1215);

        var v1216 = cloneWithChanges(v1215, Map.of(
                EntityType.PLAYER, 149
        ));
        REGISTRY.put(ProtocolVersion.V1_21_6, v1216);

        //for Protocol 771 nothing changed
        REGISTRY.put(ProtocolVersion.V1_21_7, v1216);
    }

    /**
     * Retrieves the numeric ID corresponding to the given {@code entityType}, based on
     * the current {@link ProtocolVersion} of the server. This method ensures that the correct
     * mapping of entity types to their network protocol IDs is used.
     *
     * @param entityType the {@link EntityType} for which the numeric ID is to be retrieved
     * @return the numeric ID of the specified {@code entityType}, as per the current protocol version
     * @throws IllegalArgumentException if no mapping exists for the given {@code entityType}
     *                                  in the current protocol version
     */
    public static int getEntityType(EntityType entityType) {
        ProtocolVersion version = VersionRegistry.getVersion();
        EnumMap<EntityType, Integer> map = REGISTRY.get(version);
        if (map == null || !map.containsKey(entityType)) {
            throw new IllegalArgumentException("No packet mapping for entity Type " + entityType + " in version " + version);
        }
        return map.get(entityType);
    }

    private static EnumMap<EntityType, Integer> cloneWithChanges(EnumMap<EntityType, Integer> base,
                                                               Map<EntityType, Integer> changes) {
        var clone = new EnumMap<>(base);
        clone.putAll(changes);
        return clone;
    }
}
