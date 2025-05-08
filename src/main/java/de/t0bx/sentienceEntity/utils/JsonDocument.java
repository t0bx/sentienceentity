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
