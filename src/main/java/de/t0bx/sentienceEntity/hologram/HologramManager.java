package de.t0bx.sentienceEntity.hologram;


import com.google.gson.JsonObject;
import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.utils.JsonDocument;
import de.t0bx.sentienceEntity.utils.SentienceLocation;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HologramManager {

    private final File file;
    private JsonDocument jsonDocument;

    private final Map<String, SentienceHologram> cachedHolograms = new HashMap<>();

    public HologramManager() {
        this.file = new File(SentienceEntity.getInstance().getDataFolder(), "holograms.json");
    }

    public void createHologram(String npcName, Location location, @Nullable List<String> lines) {
        if (this.cachedHolograms.containsKey(npcName)) return;

        SentienceHologram hologram = new SentienceHologram(SpigotReflectionUtil.generateEntityId(), UUID.randomUUID(), SentienceLocation.fromBukkitLocation(location));
        if (lines != null) {
            for (String line : lines) {
                hologram.addLine(line);
                this.saveLineToFile(npcName, line);
            }
        }
        this.cachedHolograms.put(npcName, hologram);
    }

    public SentienceHologram getHologram(String npcName) {
        return this.cachedHolograms.get(npcName);
    }

    public void saveLineToFile(String npcName, String text) {
        try {
            this.jsonDocument = JsonDocument.loadDocument(this.file);

            if (this.jsonDocument == null) {
                this.jsonDocument = new JsonDocument();
            }

            JsonObject npcObject;
            if (jsonDocument.hasKey(npcName)) {
                npcObject = jsonDocument.get(npcName).getAsJsonObject();
            } else {
                npcObject = new JsonObject();
                jsonDocument.set(npcName, npcObject);
            }

            int nextIndex = 0;
            while (npcObject.has(String.valueOf(nextIndex))) {
                nextIndex++;
            }

            npcObject.addProperty(String.valueOf(nextIndex), text);

            jsonDocument.save(this.file);
        } catch (IOException e) {
            SentienceEntity.getInstance().getLogger().severe("Failed to save hologram line: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String> loadLinesFromFile(String npcName) {
        this.jsonDocument = JsonDocument.loadDocument(file);

        if (this.jsonDocument == null) return null;

        if (!jsonDocument.hasKey(npcName)) return null;

        JsonObject npcObject = jsonDocument.get(npcName).getAsJsonObject();

        int index = 0;
        List<String> lines = new ArrayList<>();
        while (npcObject.has(String.valueOf(index))) {
            String lineText = npcObject.get(String.valueOf(index)).getAsString();
            lines.add(lineText);
            index++;
        }

        return lines;
    }

    public void removeLinesFromFile(String npcName) {
        try {
            this.jsonDocument = JsonDocument.loadDocument(file);

            if (this.jsonDocument == null) return;

            if (jsonDocument.hasKey(npcName)) {
                jsonDocument.remove(npcName);
                jsonDocument.save(this.file);
            }
        } catch (IOException e) {
            SentienceEntity.getInstance().getLogger().severe("Failed to remove hologram lines: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void destroyAll() {
        for (SentienceHologram hologram : this.cachedHolograms.values()) {
            hologram.destroy();
        }
    }
}