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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.network.version.VersionRegistry;
import org.bukkit.entity.EntityType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class EntityTypeRegistry {
    private static final Map<String, Integer> entityTypes = new HashMap<>();

    static {
        String version = VersionRegistry.getVersion().getVersionString();
        InputStream inputStream = SentienceEntity.getInstance().getClass().getResourceAsStream("/registries/" + version + ".json");
        if (inputStream == null) throw new RuntimeException("Could not find registry for version " + version);

        JsonObject root = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();

        JsonObject entites = root.getAsJsonObject("minecraft:entity_type").getAsJsonObject("entries");
        for (Map.Entry<String, JsonElement> entry : entites.entrySet()) {
            String key = entry.getKey();
            int id = entry.getValue().getAsJsonObject().get("protocol_id").getAsInt();

            entityTypes.put(key, id);
        }
    }

    public static int getEntityTypeId(EntityType entityType) {
        if (entityType == null) return 0;

        String entityName = "minecraft:" + entityType.name().toLowerCase();

        Integer id = entityTypes.get(entityName);
        if (id != null) return id;

        SentienceEntity.getInstance().getLogger().warning("Could not find entity type id for " + entityName);
        return 0;
    }
}
