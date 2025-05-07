package de.t0bx.sentienceEntity.hologram;


import com.google.gson.JsonObject;
import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.npc.NPCsHandler;
import de.t0bx.sentienceEntity.utils.JsonDocument;
import de.t0bx.sentienceEntity.utils.SentienceLocation;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HologramManager {

    private final File file;
    private JsonDocument jsonDocument;
    private final NPCsHandler npcshandler;

    private final Map<String, SentienceHologram> cachedHolograms = new ConcurrentHashMap<>();

    public HologramManager() {
        this.file = new File(SentienceEntity.getInstance().getDataFolder(), "holograms.json");
        this.npcshandler = SentienceEntity.getInstance().getNpcshandler();

        for (String npcNames : this.npcshandler.getNPCNames()) {
            this.loadLinesFromFile(npcNames);
        }
    }

    public void showAllHolograms(Player player) {
        for (Map.Entry<String, SentienceHologram> hologramEntry : cachedHolograms.entrySet()) {
            SentienceHologram hologram = hologramEntry.getValue();
            hologram.spawn(player);
        }
    }

    public void createHologram(String npcName, SentienceLocation location) {
        if (this.cachedHolograms.containsKey(npcName)) return;

        SentienceHologram hologram = new SentienceHologram(SpigotReflectionUtil.generateEntityId(), UUID.randomUUID(), location);
        this.cachedHolograms.put(npcName, hologram);
    }

    public void addLine(String npcName, String line) {
        if (!this.cachedHolograms.containsKey(npcName)) return;

        SentienceHologram hologram = this.cachedHolograms.get(npcName);
        hologram.addLine(line);
        this.saveLineToFile(npcName, line);
    }

    public void show(Player player, String npcName) {
        if (!this.cachedHolograms.containsKey(npcName)) return;

        SentienceHologram hologram = this.cachedHolograms.get(npcName);
        hologram.spawn(player);
    }

    public void updateLine(String npcName, int index, String text) {
        if (!this.cachedHolograms.containsKey(npcName)) return;

        SentienceHologram hologram = this.cachedHolograms.get(npcName);
        hologram.updateLine(index, text);
        this.updateLineInFile(npcName, index, text);
    }

    public boolean doesLineExist(String npcName, int index) {
        if (!this.cachedHolograms.containsKey(npcName)) return false;

        SentienceHologram hologram = this.cachedHolograms.get(npcName);

        return hologram.getHologramLines().containsKey(index);
    }

    public void removeLine(String npcName, int index) {
        if (!this.cachedHolograms.containsKey(npcName)) return;

        SentienceHologram hologram = this.cachedHolograms.get(npcName);
        hologram.removeLine(index);
        this.removeLineFromFile(npcName, index);
    }

    public void removeHologram(String npcName) {
        if (!this.cachedHolograms.containsKey(npcName)) return;

        SentienceHologram hologram = this.cachedHolograms.get(npcName);
        hologram.destroy();
        this.cachedHolograms.remove(npcName);
        this.removeLinesFromFile(npcName);
    }

    public SentienceHologram getHologram(String npcName) {
        return this.cachedHolograms.get(npcName);
    }

    public Map<Integer, HologramLine> getHologramLines(String npcName) {
        return this.cachedHolograms.get(npcName).getHologramLines();
    }

    private void saveLineToFile(String npcName, String text) {
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

    private void removeLineFromFile(String npcName, int index) {
        try {
            this.jsonDocument = JsonDocument.loadDocument(this.file);

            if (this.jsonDocument == null) return;

            if (!this.jsonDocument.hasKey(npcName)) return;

            JsonObject npcObject = this.jsonDocument.get(npcName).getAsJsonObject();

            if (!npcObject.has(String.valueOf(index))) return;

            npcObject.remove(String.valueOf(index));

            int currentIndex = index + 1;
            while (npcObject.has(String.valueOf(currentIndex))) {
                String lineText = npcObject.get(String.valueOf(currentIndex)).getAsString();
                npcObject.remove(String.valueOf(currentIndex));
                npcObject.addProperty(String.valueOf(currentIndex - 1), lineText);
                currentIndex++;
            }

            this.jsonDocument.save(this.file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void updateLineInFile(String npcName, int index, String text) {
        try {
            this.jsonDocument = JsonDocument.loadDocument(this.file);

            if (this.jsonDocument == null) return;

            if (!this.jsonDocument.hasKey(npcName)) return;

            JsonObject npcObject = this.jsonDocument.get(npcName).getAsJsonObject();

            if (!npcObject.has(String.valueOf(index))) return;

            npcObject.addProperty(String.valueOf(index), text);

            this.jsonDocument.save(this.file);
        } catch (IOException e) {
            SentienceEntity.getInstance().getLogger().severe("Failed to update hologram line: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLinesFromFile(String npcName) {
        this.jsonDocument = JsonDocument.loadDocument(file);

        if (this.jsonDocument == null) return;

        if (!jsonDocument.hasKey(npcName)) return;

        JsonObject npcObject = jsonDocument.get(npcName).getAsJsonObject();

        SentienceHologram hologram = new SentienceHologram(
                SpigotReflectionUtil.generateEntityId(),
                UUID.randomUUID(),
                this.npcshandler.getNPC(npcName).getLocation()
        );

        int index = 0;
        while (npcObject.has(String.valueOf(index))) {
            String lineText = npcObject.get(String.valueOf(index)).getAsString();
            hologram.addLine(lineText);
            index++;
        }

        this.cachedHolograms.put(npcName, hologram);
    }

    private void removeLinesFromFile(String npcName) {
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