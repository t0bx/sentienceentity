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

package de.t0bx.sentienceEntity.network.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.EnumMap;
import java.util.Map;

public class AdventureSerializer {
    private static final TextDecoration[] DECORATIONS = {
            TextDecoration.BOLD,
            TextDecoration.ITALIC,
            TextDecoration.UNDERLINED,
            TextDecoration.STRIKETHROUGH,
            TextDecoration.OBFUSCATED
    };

    /**
     * Serializes the given {@code Component} into a {@code JsonObject}
     * while applying conditional decorations such as text styles.
     *
     * @param component the {@code Component} to be serialized
     * @return a {@code JsonObject} representing the serialized component with applied decorations
     */
    public static JsonObject serialize(Component component) {
        String json = GsonComponentSerializer.gson().serialize(component);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        Map<TextDecoration, Boolean> defaultDecorations = new EnumMap<>(TextDecoration.class);
        for (TextDecoration decoration : DECORATIONS) {
            defaultDecorations.put(decoration, false);
        }

        return applyConditionalDecorations(root, defaultDecorations);
    }

    /**
     * Applies conditional text decorations (such as bold, italic, etc.) to a given JsonObject
     * based on inherited decoration values and the object's own properties.
     *
     * @param obj the JsonObject to which conditional decorations will be applied
     * @param inherited a map of TextDecoration keys to their boolean values representing inherited states
     * @return a new JsonObject with conditional decorations applied
     */
    private static JsonObject applyConditionalDecorations(JsonObject obj, Map<TextDecoration, Boolean> inherited) {
        JsonObject copy = obj.deepCopy();
        Map<TextDecoration, Boolean> local = new EnumMap<>(inherited);

        for (TextDecoration decoration : DECORATIONS) {
            String key = decoration.toString().toLowerCase();

            if (copy.has(key)) {
                boolean value = copy.get(key).getAsBoolean();
                local.put(decoration, value);
            } else {
                boolean inheritedVal = inherited.getOrDefault(decoration, false);
                if (inheritedVal) {
                    copy.addProperty(key, false);
                    local.put(decoration, false);
                }
            }
        }

        if (copy.has("extra")) {
            JsonArray original = copy.getAsJsonArray("extra");
            JsonArray fixed = new JsonArray();

            for (JsonElement el : original) {
                if (el.isJsonObject()) {
                    fixed.add(applyConditionalDecorations(el.getAsJsonObject(), local));
                } else if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                    JsonObject wrapper = new JsonObject();
                    wrapper.addProperty("text", el.getAsString());
                    fixed.add(applyConditionalDecorations(wrapper, local));
                } else {
                    fixed.add(el);
                }
            }

            copy.add("extra", fixed);
        }

        return copy;
    }
}
