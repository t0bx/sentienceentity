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

import de.t0bx.sentienceEntity.network.utils.PacketId;
import de.t0bx.sentienceEntity.network.version.ProtocolVersion;
import de.t0bx.sentienceEntity.network.version.VersionRegistry;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PacketIdRegistry {
    private static final Map<ProtocolVersion, EnumMap<PacketId, Integer>> REGISTRY = new HashMap<>();

    static {
        var v1_21 = new EnumMap<PacketId, Integer>(PacketId.class);
        v1_21.put(PacketId.PLAYER_INFO_UPDATE, 0x3E);
        v1_21.put(PacketId.PLAYER_INFO_REMOVE, 0x3D);
        v1_21.put(PacketId.SPAWN_ENTITY, 0x01);
        v1_21.put(PacketId.SET_ENTITY_METADATA, 0x58);
        v1_21.put(PacketId.SET_HEAD_ROTATION, 0x48);
        v1_21.put(PacketId.UPDATE_ENTITY_ROTATION, 0x30);
        v1_21.put(PacketId.REMOVE_ENTITY, 0x42);
        v1_21.put(PacketId.TELEPORT_ENTITY, 0x70);
        v1_21.put(PacketId.INTERACT_ENTITY, 0x16);
        v1_21.put(PacketId.SET_PLAYER_TEAM, 0x60);
        REGISTRY.put(ProtocolVersion.V1_21, v1_21);

        //No Changes from v1_21 -> v1_21_1
        REGISTRY.put(ProtocolVersion.V1_21_1, v1_21);

        var v1_21_2 = cloneWithChanges(v1_21, Map.of(
                PacketId.PLAYER_INFO_UPDATE, 0x40,
                PacketId.PLAYER_INFO_REMOVE, 0x3F,
                PacketId.SET_ENTITY_METADATA, 0x5D,
                PacketId.SET_HEAD_ROTATION, 0x4D,
                PacketId.UPDATE_ENTITY_ROTATION, 0x32,
                PacketId.REMOVE_ENTITY, 0x47,
                PacketId.TELEPORT_ENTITY, 0x20,
                PacketId.INTERACT_ENTITY, 0x18,
                PacketId.SET_PLAYER_TEAM, 0x67
        ));
        REGISTRY.put(ProtocolVersion.V1_21_2, v1_21_2);

        //No Changes from v1_21_2 -> v1_21_3
        REGISTRY.put(ProtocolVersion.V1_21_3, v1_21_2);

        //No Changes from v1_21_3 -> v1_21_4
        REGISTRY.put(ProtocolVersion.V1_21_4, v1_21_2);

        var v1_21_5 = cloneWithChanges(v1_21_2, Map.of(
                PacketId.PLAYER_INFO_UPDATE, 0x3F,
                PacketId.PLAYER_INFO_REMOVE, 0x3E,
                PacketId.SET_ENTITY_METADATA, 0x5C,
                PacketId.SET_HEAD_ROTATION, 0x4C,
                PacketId.UPDATE_ENTITY_ROTATION, 0x31,
                PacketId.REMOVE_ENTITY, 0x46,
                PacketId.TELEPORT_ENTITY, 0x1F,
                PacketId.SET_PLAYER_TEAM, 0x66
        ));
        REGISTRY.put(ProtocolVersion.V1_21_5, v1_21_5);

        var v1_21_6 = cloneWithChanges(v1_21_5, Map.of(
                PacketId.INTERACT_ENTITY, 0x19
        ));
        REGISTRY.put(ProtocolVersion.V1_21_6, v1_21_6);

        // No changes for 1_21_7 from 1_21_6
        REGISTRY.put(ProtocolVersion.V1_21_7, v1_21_6);
    }

    /**
     * Retrieves the numeric packet ID associated with the specified {@code PacketId}, based on
     * the current {@code ProtocolVersion} of the server. This method ensures that the mapping
     * of protocol versions to packet IDs is respected.
     *
     * @param packetId the identifier of the packet for which the numeric ID is to be retrieved
     * @return the numeric ID of the specified packet, as per the current protocol version
     * @throws IllegalArgumentException if no mapping exists for the given {@code packetId}
     *                                  in the current protocol version
     */
    public static int getPacketId(PacketId packetId) {
        ProtocolVersion version = VersionRegistry.getVersion();
        EnumMap<PacketId, Integer> map = REGISTRY.get(version);
        if (map == null || !map.containsKey(packetId)) {
            throw new IllegalArgumentException("No packet mapping for packet id " + packetId + " in version " + version);
        }
        return map.get(packetId);
    }

    private static EnumMap<PacketId, Integer> cloneWithChanges(EnumMap<PacketId, Integer> base,
                                                               Map<PacketId, Integer> changes) {
        var clone = new EnumMap<>(base);
        clone.putAll(changes);
        return clone;
    }
}
