package de.t0bx.sentienceEntity.boundingbox;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.t0bx.sentienceEntity.SentienceEntity;
import org.bukkit.entity.EntityType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class BoundingBoxRegistry {
    private static final Map<EntityType, BoundingBox> boundingBoxes = new HashMap<>();

    static {
        InputStream inputStream = SentienceEntity.getInstance()
                .getClass()
                .getResourceAsStream("/registries/hitbox_sizes.json");
        if (inputStream == null)
            throw new RuntimeException("Could not find hitbox_sizes.json");

        JsonObject root = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String entityName = entry.getKey();
            JsonObject variantsObj = entry.getValue().getAsJsonObject();

            EntityType type;
            try {
                type = EntityType.valueOf(entityName.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown EntityType: " + entityName);
                continue;
            }

            BoundingBox box = null;

            if (variantsObj.has("single")) {
                JsonObject boxObj = variantsObj.getAsJsonObject("single");
                box = parseBoundingBox(boxObj);
            }
            else if (variantsObj.has("adult")) {
                JsonObject boxObj = variantsObj.getAsJsonObject("adult");
                box = parseBoundingBox(boxObj);
            }

            if (box != null) {
                boundingBoxes.put(type, box);
            }
        }
    }

    private static BoundingBox parseBoundingBox(JsonObject obj) {
        double height = obj.get("height").getAsDouble();
        double width = obj.get("width").getAsDouble();
        return new BoundingBox(width, height);
    }

    public static BoundingBox getBoundingBox(EntityType type) {
        return boundingBoxes.get(type);
    }

    public record BoundingBox(double width, double height) {}
}
