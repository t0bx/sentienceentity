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

package de.t0bx.sentienceEntity.utils;

import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public class JsonDocument {

    @Getter
    @Setter
    private JsonObject jsonObject;

    private static final JsonParser parser = new JsonParser();

    private final Gson gson;

    public JsonDocument() {
        this.jsonObject = new JsonObject();
        this.gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    }

    public JsonDocument(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        this.gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    }

    public static JsonDocument loadDocument(File file) {
        try{
            return new JsonDocument((JsonObject) parser.parse(new FileReader(file)));
        }catch(Exception e){
            return null;
        }
    }

    public void save(File file) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }

        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(jsonObject, outputStreamWriter);
        }
    }

    public JsonElement get(String key) {
        return jsonObject.get(key);
    }

    public void set(String key, JsonElement value) {
        jsonObject.add(key, value);
    }

    public void setString(String key, String value) {
        jsonObject.addProperty(key, value);
    }

    public void setNumber(String key, Number value) {
        jsonObject.addProperty(key, value);
    }

    public void setBoolean(String key, boolean value) {
        jsonObject.addProperty(key, value);
    }

    public void remove(String key) {
        jsonObject.remove(key);
    }

    public boolean hasKey(String key) {
        return jsonObject.has(key);
    }

    public Set<String> getKeys() {
        return jsonObject.keySet();
    }

    public void update(String path, Object value) {
        try {
            String[] pathParts = path.split("\\.");
            JsonElement current = jsonObject;

            for (int i = 0; i < pathParts.length - 1; i++) {
                String part = pathParts[i];

                if (current instanceof JsonObject currentObj) {
                    if (!currentObj.has(part) || !currentObj.get(part).isJsonObject()) {
                        currentObj.add(part, new JsonObject());
                    }

                    current = currentObj.get(part);
                } else {
                    return;
                }
            }

            String lastPart = pathParts[pathParts.length - 1];
            if (current instanceof JsonObject parentObj) {
                switch (value) {
                    case String s -> parentObj.addProperty(lastPart, s);
                    case Number number -> parentObj.addProperty(lastPart, number);
                    case Boolean b -> parentObj.addProperty(lastPart, b);
                    case JsonElement element -> parentObj.add(lastPart, element);
                    case null -> parentObj.add(lastPart, JsonNull.INSTANCE);
                    default -> parentObj.add(lastPart, gson.toJsonTree(value));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<Map.Entry<String, JsonElement>> getEntries() {
        return jsonObject.entrySet();
    }

    @Override
    public String toString() {
        return gson.toJson(jsonObject);
    }
}
