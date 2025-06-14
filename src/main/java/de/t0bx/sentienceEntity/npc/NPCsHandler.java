/**
 *Creative Commons Attribution-NonCommercial 4.0 International Public License
 * By using this code, you agree to the following terms:
 * You are free to:
 * - Share — copy and redistribute the material in any medium or format
 * - Adapt — remix, transform, and build upon the material
 * Under the following terms:
 * 1. Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made.
 * 2. NonCommercial — You may not use the material for commercial purposes.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 * Full License Text: https://creativecommons.org/licenses/by-nc/4.0/legalcode
 * ---
 * Copyright (c) 2025 t0bx
 * This work is licensed under the Creative Commons Attribution-NonCommercial 4.0 International License.
 */

package de.t0bx.sentienceEntity.npc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.utils.JsonDocument;
import de.t0bx.sentienceEntity.utils.ReflectionUtils;
import de.t0bx.sentienceEntity.utils.SkinFetcher;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NPCsHandler {

    private final Map<String, SentienceNPC> npcCache;
    private final Map<Integer, String> npcIdCache;
    @Getter
    private final Set<Integer> npcIds;
    private JsonDocument jsonDocument;
    private final SkinFetcher skinFetcher;
    private final File file;

    public NPCsHandler() {
        this.npcCache = new HashMap<>();
        this.npcIdCache = new HashMap<>();
        this.npcIds = new HashSet<>();
        this.skinFetcher = SentienceEntity.getInstance().getSkinFetcher();
        this.file = new File(SentienceEntity.getInstance().getDataFolder(), "npcs.json");
        this.loadNPCsFromFile();
    }

    public void createNPC(String npcName, String playerName, Location location, boolean persistent) {
        UUID npcUUID = UUID.randomUUID();

        this.skinFetcher.fetchSkin(playerName, (skinValue, skinSignature) -> {
            if (skinValue == null && skinSignature == null) return;

            GameProfile gameProfile = new GameProfile(npcUUID, "");
            gameProfile.getProperties().clear();
            gameProfile.getProperties().put("textures", new Property("textures", skinValue, skinSignature));

            SentienceNPC npc = new SentienceNPC(ReflectionUtils.generateValidMinecraftEntityId(), gameProfile);
            npc.setLocation(location);

            this.npcCache.put(npcName, npc);
            this.npcIdCache.put(npc.getEntityId(), npcName);
            this.npcIds.add(npc.getEntityId());

            if (persistent) {
                this.saveNPCtoFile(npcName, location, skinValue, skinSignature);
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                this.spawnNPC(npcName, player);
            }
        });
    }

    public void createNPC(String npcName, String playerName, Location location) {
        UUID npcUUID = UUID.randomUUID();

        this.skinFetcher.fetchSkin(playerName, (skinValue, skinSignature) -> {
            if (skinValue == null && skinSignature == null) return;

            GameProfile gameProfile = new GameProfile(npcUUID, "");
            gameProfile.getProperties().clear();
            gameProfile.getProperties().put("textures", new Property("textures", skinValue, skinSignature));

            SentienceNPC npc = new SentienceNPC(ReflectionUtils.generateValidMinecraftEntityId(), gameProfile);
            npc.setLocation(location);

            this.npcCache.put(npcName, npc);
            this.npcIdCache.put(npc.getEntityId(), npcName);
            this.npcIds.add(npc.getEntityId());
            this.saveNPCtoFile(npcName, location, skinValue, skinSignature);
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.spawnNPC(npcName, player);
            }
        });
    }

    public void createNPC(String npcName, Location location, String skinValue, String skinSignature) {
        UUID npcUUID = UUID.randomUUID();

        GameProfile gameProfile = new GameProfile(npcUUID, "");
        gameProfile.getProperties().clear();
        gameProfile.getProperties().put("textures", new Property("textures", skinValue, skinSignature));

        SentienceNPC npc = new SentienceNPC(ReflectionUtils.generateValidMinecraftEntityId(), gameProfile);
        npc.setLocation(location);

        this.npcCache.put(npcName, npc);
        this.npcIdCache.put(npc.getEntityId(), npcName);
        this.npcIds.add(npc.getEntityId());
        this.saveNPCtoFile(npcName, location, skinValue, skinSignature);
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.spawnNPC(npcName, player);
        }
    }

    public void createNPC(String npcName, String playerName, Location location, Runnable callback) {
        UUID npcUUID = UUID.randomUUID();

        this.skinFetcher.fetchSkin(playerName, (skinValue, skinSignature) -> {
            if (skinValue == null && skinSignature == null) {
                if (callback != null) callback.run();
                return;
            }

            GameProfile gameProfile = new GameProfile(npcUUID, "");
            gameProfile.getProperties().clear();
            gameProfile.getProperties().put("textures", new Property("textures", skinValue, skinSignature));

            SentienceNPC npc = new SentienceNPC(ReflectionUtils.generateValidMinecraftEntityId(), gameProfile);
            npc.setLocation(location);

            this.npcCache.put(npcName, npc);
            this.npcIdCache.put(npc.getEntityId(), npcName);
            this.npcIds.add(npc.getEntityId());
            this.saveNPCtoFile(npcName, location, skinValue, skinSignature);

            for (Player player : Bukkit.getOnlinePlayers()) {
                this.spawnNPC(npcName, player);
            }

            if (callback != null) {
                callback.run();
            }
        });
    }

    public void removeNPC(String npcName) {
        this.jsonDocument = JsonDocument.loadDocument(this.file);
        if (this.jsonDocument == null) return;
        if (!this.npcCache.containsKey(npcName)) return;

        SentienceNPC npc = this.npcCache.remove(npcName);
        npc.despawnAll();
        this.npcIds.remove(npc.getEntityId());
        this.npcIdCache.remove(npc.getEntityId());

        this.jsonDocument.remove(npcName);

        try {
            this.jsonDocument.save(this.file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public List<String> getNPCNames() {
        return new ArrayList<>(this.npcCache.keySet());
    }

    public int getLoadedSize() {
        return this.npcCache.size();
    }

    private void loadNPCsFromFile() {
        this.jsonDocument = JsonDocument.loadDocument(this.file);

        if (this.jsonDocument == null || this.jsonDocument.getJsonObject() == null) {
            return;
        }

        JsonObject allNPCs = this.jsonDocument.getJsonObject();

        for (Map.Entry<String, JsonElement> entry : allNPCs.entrySet()) {
            String npcName = entry.getKey();
            JsonObject data = entry.getValue().getAsJsonObject();

            double x = data.get("location-x").getAsDouble();
            double y = data.get("location-y").getAsDouble();
            double z = data.get("location-z").getAsDouble();
            float yaw = data.get("location-yaw").getAsFloat();
            float pitch = data.get("location-pitch").getAsFloat();
            String worldName = data.get("location-world").getAsString();

            if (Bukkit.getWorld(worldName) == null) continue;

            Location location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);

            String skinValue = data.get("skin-value").getAsString();
            String skinSignature = data.get("skin-signature").getAsString();

            UUID npcUUID = UUID.randomUUID();

            GameProfile gameProfile = new GameProfile(npcUUID, "");
            gameProfile.getProperties().clear();
            gameProfile.getProperties().put("textures", new Property("textures", skinValue, skinSignature));

            SentienceNPC npc = new SentienceNPC(ReflectionUtils.generateValidMinecraftEntityId(), gameProfile);
            npc.setLocation(location);

            JsonObject settings = data.getAsJsonObject("settings");
            npc.setShouldLookAtPlayer(settings.get("shouldLookAtPlayer").getAsBoolean());
            npc.setShouldSneakWithPlayer(settings.get("shouldSneakWithPlayer").getAsBoolean());

            this.npcCache.put(npcName, npc);
            this.npcIds.add(npc.getEntityId());
            this.npcIdCache.put(npc.getEntityId(), npcName);
        }
    }

    public SentienceNPC getNPC(String npcName) {
        return this.npcCache.get(npcName);
    }

    public Set<SentienceNPC> getAllNPCs() {
        Set<SentienceNPC> npcs = new HashSet<>();
        for (Map.Entry<String, SentienceNPC> entry : this.npcCache.entrySet()) {
            npcs.add(entry.getValue());
        }
        return npcs;
    }

    public String getNpcNameFromId(int entityId) {
        return this.npcIdCache.getOrDefault(entityId, null);
    }

    public Map<String, SentienceNPC> getNPCMap() {
        return this.npcCache;
    }

    public boolean doesNPCExist(String npcName) {
        return this.npcCache.containsKey(npcName);
    }

    public void updateSkin(String npcName, String playerName) {
        this.jsonDocument = JsonDocument.loadDocument(this.file);
        SentienceNPC npc = this.npcCache.get(npcName);
        if (npc == null) return;

        this.skinFetcher.fetchSkin(playerName, (skinValue, skinSignature) -> {
            if (skinValue == null && skinSignature == null) return;

            this.jsonDocument.update(npcName + ".skin-value", skinValue);
            this.jsonDocument.update(npcName + ".skin-signature", skinSignature);

            npc.changeSkin(skinValue, skinSignature);

            try {
                this.jsonDocument.save(this.file);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateLocation(String npcName, Location location) {
        this.jsonDocument = JsonDocument.loadDocument(this.file);
        SentienceNPC npc = this.npcCache.get(npcName);
        if (this.jsonDocument == null) return;
        if (npc == null) return;

        npc.teleport(location);

        this.jsonDocument.update(npcName + ".location-x", location.getX());
        this.jsonDocument.update(npcName + ".location-y", location.getY());
        this.jsonDocument.update(npcName + ".location-z", location.getZ());
        this.jsonDocument.update(npcName + ".location-yaw", location.getYaw());
        this.jsonDocument.update(npcName + ".location-pitch", location.getPitch());
        this.jsonDocument.update(npcName + ".location-world", location.getWorld().getName());

        try {
            this.jsonDocument.save(this.file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public String updateLookAtPlayer(String npcName) {
        this.jsonDocument = JsonDocument.loadDocument(this.file);
        SentienceNPC npc = this.npcCache.get(npcName);
        if (npc == null) return "error";
        if (jsonDocument == null) return "error";

        boolean newValue = !npc.isShouldLookAtPlayer();
        npc.setShouldLookAtPlayer(newValue);
        this.jsonDocument.update(npcName + ".settings.shouldLookAtPlayer", newValue);
        try {
            this.jsonDocument.save(this.file);
        } catch (IOException exception) {
            exception.printStackTrace();
            return "error";
        }

        if (newValue) {
            return "true";
        }
        return "false";
    }

    public String updateSneakWithPlayer(String npcName) {
        this.jsonDocument = JsonDocument.loadDocument(this.file);
        SentienceNPC npc = this.npcCache.get(npcName);
        if (npc == null) return "error";
        if (jsonDocument == null) return "error";

        boolean newValue = !npc.isShouldSneakWithPlayer();
        npc.setShouldSneakWithPlayer(newValue);
        this.jsonDocument.update(npcName + ".settings.shouldSneakWithPlayer", newValue);
        try {
            this.jsonDocument.save(this.file);
        } catch (IOException exception) {
            exception.printStackTrace();
            return "error";
        }

        if (newValue) {
            return "true";
        }
        return "false";
    }

    public void spawnNPC(String npcName, Player player) {
        SentienceNPC npc = this.npcCache.get(npcName);
        if (npc == null) return;

        npc.spawn(player);
    }

    public void spawnAllNPCs(Player player) {
        if (player == null || !player.isOnline()) return;

        this.npcCache.values().stream()
                .filter(Objects::nonNull)
                .forEach(npc -> npc.spawn(player));
    }

    public void despawnAllNPCs(Player player) {
        if (player == null || !player.isOnline()) return;

        this.npcCache.values().stream()
                .filter(Objects::nonNull)
                .forEach(npc -> npc.despawn(player));
    }

    private void saveNPCtoFile(String npcName, Location location, String skinValue, String skinSignature) {
        try {
            this.jsonDocument = JsonDocument.loadDocument(this.file);

            if (this.jsonDocument == null) {
                this.jsonDocument = new JsonDocument();
            }

            JsonObject existingData = this.jsonDocument.getJsonObject();
            if (existingData == null) {
                existingData = new JsonObject();
            }

            JsonObject npcObject = this.getJsonObject(location, skinValue, skinSignature);
            existingData.add(npcName, npcObject);

            this.jsonDocument.setJsonObject(existingData);
            this.jsonDocument.save(this.file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private @NotNull JsonObject getJsonObject(Location location, String skinValue, String skinSignature) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("location-x", location.getX());
        jsonObject.addProperty("location-y", location.getY());
        jsonObject.addProperty("location-z", location.getZ());
        jsonObject.addProperty("location-yaw", location.getYaw());
        jsonObject.addProperty("location-pitch", location.getPitch());
        jsonObject.addProperty("location-world", location.getWorld().getName());
        jsonObject.addProperty("skin-value", skinValue);
        jsonObject.addProperty("skin-signature", skinSignature);

        JsonObject settingsObject = new JsonObject();
        settingsObject.addProperty("shouldLookAtPlayer", false);
        settingsObject.addProperty("shouldSneakWithPlayer", false);
        jsonObject.add("settings", settingsObject);
        return jsonObject;
    }
}