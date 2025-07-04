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

package de.t0bx.sentienceEntity.hologram;

import com.google.gson.JsonObject;
import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.npc.NpcsHandler;
import de.t0bx.sentienceEntity.utils.JsonDocument;
import de.t0bx.sentienceEntity.utils.ReflectionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HologramManager {

    private final File file;
    private JsonDocument jsonDocument;
    private final NpcsHandler npcshandler;

    private final Map<String, SentienceHologram> cachedHolograms = new ConcurrentHashMap<>();

    public HologramManager() {
        this.file = new File(SentienceEntity.getInstance().getDataFolder(), "holograms.json");
        this.npcshandler = SentienceEntity.getInstance().getNpcshandler();

        for (String npcNames : this.npcshandler.getNPCNames()) {
            this.loadLinesFromFile(npcNames);
        }
    }

    /**
     * Displays all cached holograms to the specified player.
     *
     * This method iterates through all holograms stored in the `cachedHolograms`
     * map and spawns each one for the provided player.
     *
     * @param player the player for whom the holograms will be displayed
     */
    public void showAllHolograms(Player player) {
        for (Map.Entry<String, SentienceHologram> hologramEntry : cachedHolograms.entrySet()) {
            SentienceHologram hologram = hologramEntry.getValue();
            hologram.spawn(player);
        }
    }

    /**
     * Hides all cached holograms from the specified player.
     *
     * This method iterates through all holograms stored in the
     * `cachedHolograms` map and despawns each one for the provided player.
     *
     * @param player the player from whom the holograms will be hidden
     */
    public void unShowAllHolograms(Player player) {
        for (Map.Entry<String, SentienceHologram> hologramEntry : cachedHolograms.entrySet()) {
            SentienceHologram hologram = hologramEntry.getValue();
            hologram.despawn(player);
        }
    }

    /**
     * Creates a new hologram for the specified NPC at the given location.
     * If a hologram with the specified NPC name already exists, this method does nothing.
     *
     * @param npcName the name of the NPC for which the hologram is being created
     * @param location the location at which the hologram will be displayed
     */
    public void createHologram(String npcName, Location location) {
        if (this.cachedHolograms.containsKey(npcName)) return;

        SentienceHologram hologram = new SentienceHologram(ReflectionUtils.generateValidMinecraftEntityId(), UUID.randomUUID(), location);
        this.cachedHolograms.put(npcName, hologram);
    }

    /**
     * Adds a line of text to the hologram associated with the specified NPC.
     *
     * If the hologram for the given NPC name exists, the line will be added to it.
     * If the persistent flag is set to true, the line will also be saved to a file.
     *
     * @param npcName the name of the NPC whose hologram will have the line added
     * @param line the text content of the line to be added to the hologram
     * @param persistent whether the line should be saved persistently to a file
     */
    public void addLine(String npcName, String line, boolean persistent) {
        if (!this.cachedHolograms.containsKey(npcName)) return;

        SentienceHologram hologram = this.cachedHolograms.get(npcName);
        hologram.addLine(line);
        if (persistent) {
            this.saveLineToFile(npcName, line);
        }
    }

    /**
     * Displays a specific hologram to the given player.
     *
     * If a hologram associated with the specified NPC name exists in the cached
     * holograms, this method spawns it for the provided player.
     *
     * @param player the player for whom the hologram will be displayed
     * @param npcName the name of the NPC associated with the hologram to display
     */
    public void show(Player player, String npcName) {
        if (!this.cachedHolograms.containsKey(npcName)) return;

        SentienceHologram hologram = this.cachedHolograms.get(npcName);
        hologram.spawn(player);
    }

    /**
     * Updates a specific line of text in the hologram associated with the specified NPC.
     *
     * If the NPC's hologram exists in the cache, this method updates the text of the line
     * at the specified index and saves the updated line to the file.
     *
     * @param npcName the name of the NPC whose hologram line will be updated
     * @param index the index of the line to update in the hologram
     * @param text the new text to set for the specified line
     */
    public void updateLine(String npcName, int index, String text) {
        if (!this.cachedHolograms.containsKey(npcName)) return;

        SentienceHologram hologram = this.cachedHolograms.get(npcName);
        hologram.updateLine(index, text);
        this.updateLineInFile(npcName, index, text);
    }

    /**
     * Checks whether a specific line exists in the hologram associated with the given NPC name.
     *
     * @param npcName the name of the NPC whose hologram is being checked
     * @param index the index of the line to check in the hologram
     * @return true if the line exists in the hologram; false otherwise
     */
    public boolean doesLineExist(String npcName, int index) {
        if (!this.cachedHolograms.containsKey(npcName)) return false;

        SentienceHologram hologram = this.cachedHolograms.get(npcName);

        return hologram.getHologramLines().containsKey(index);
    }

    /**
     * Removes a specific line of text from the hologram associated with the specified NPC.
     *
     * If the hologram for the given NPC exists, the line at the specified index will be removed
     * from both the hologram cache and the associated file storage.
     *
     * @param npcName the name of the NPC whose hologram line will be removed
     * @param index the index of the line to remove in the hologram
     */
    public void removeLine(String npcName, int index) {
        if (!this.cachedHolograms.containsKey(npcName)) return;

        SentienceHologram hologram = this.cachedHolograms.get(npcName);
        hologram.removeLine(index);
        this.removeLineFromFile(npcName, index);
    }

    /**
     * Removes the hologram associated with the specified NPC name.
     *
     * This method removes the hologram from the cache, destroys it,
     * and deletes any related data from the corresponding file storage.
     *
     * @param npcName the name of the NPC whose hologram will be removed
     */
    public void removeHologram(String npcName) {
        if (!this.cachedHolograms.containsKey(npcName)) return;

        SentienceHologram hologram = this.cachedHolograms.get(npcName);
        hologram.destroy();
        this.cachedHolograms.remove(npcName);
        this.removeLinesFromFile(npcName);
    }

    /**
     * Retrieves the hologram associated with the specified NPC name.
     *
     * This method looks up the cached holograms map to find and return the hologram
     * associated with the provided NPC name. If no hologram is found for the given
     * NPC name, this method returns null.
     *
     * @param npcName the name of the NPC whose hologram is to be retrieved
     * @return the SentienceHologram object associated with the specified NPC name,
     *         or null if no hologram exists for the given NPC name
     */
    public SentienceHologram getHologram(String npcName) {
        return this.cachedHolograms.get(npcName);
    }

    /**
     * Retrieves a map of hologram lines associated with a specific NPC name.
     *
     * The returned map contains hologram lines where the keys are the line indices
     * and the values are the corresponding {@code HologramLine} objects. If the given
     * NPC name does not have an associated hologram, the returned map may be empty or null.
     *
     * @param npcName the name of the NPC whose hologram lines are to be retrieved
     * @return a {@code Map<Integer, HologramLine>} where keys represent line indices and
     *         values are the associated {@code HologramLine} objects
     */
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
                ReflectionUtils.generateValidMinecraftEntityId(),
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