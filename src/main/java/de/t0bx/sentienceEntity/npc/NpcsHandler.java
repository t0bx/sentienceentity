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
import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.network.utils.NpcProfile;
import de.t0bx.sentienceEntity.network.wrapper.packets.PacketPlayerInfoUpdate;
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

public class NpcsHandler {

    private final Map<String, SentienceNPC> npcCache;
    private final Map<Integer, String> npcIdCache;
    @Getter
    private final Set<Integer> npcIds;
    private JsonDocument jsonDocument;
    private final SkinFetcher skinFetcher;
    private final File file;

    public NpcsHandler() {
        this.npcCache = new HashMap<>();
        this.npcIdCache = new HashMap<>();
        this.npcIds = new HashSet<>();
        this.skinFetcher = SentienceEntity.getInstance().getSkinFetcher();
        this.file = new File(SentienceEntity.getInstance().getDataFolder(), "npcs.json");
        this.loadNPCsFromFile();
    }

    /**
     * Creates a new NPC (Non-Player Character) in the game with the specified name, skin based on the player's name,
     * and location. The NPC can optionally be persistent, meaning it will be saved to a file for future use.
     *
     * @param npcName      The name of the NPC to be created.
     * @param playerName   The name of the player whose skin will be used for the NPC.
     * @param location     The location where the NPC will be spawned.
     * @param persistent   Whether the NPC should be saved persistently for future use.
     */
    public void createNPC(String npcName, String playerName, Location location, boolean persistent) {
        UUID npcUUID = UUID.randomUUID();

        this.skinFetcher.fetchSkin(playerName, (skinValue, skinSignature) -> {
            if (skinValue == null && skinSignature == null) return;

            int npcEntityId = ReflectionUtils.generateValidMinecraftEntityId();

            List<PacketPlayerInfoUpdate.Property> properties = new ArrayList<>();
            properties.add(new PacketPlayerInfoUpdate.Property("textures", skinValue, skinSignature));
            NpcProfile npcProfile = new NpcProfile("", npcEntityId, npcUUID, properties);

            SentienceNPC npc = new SentienceNPC(npcEntityId, npcProfile);
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

    /**
     * Creates a new NPC (Non-Player Character) using the specified name, player's skin, and location.
     * The NPC's appearance is based on the skin of the given player and will be spawned
     * at the specified location. The NPC is also saved to a cache and made visible to all online players.
     *
     * @param npcName    The name of the NPC to be created.
     * @param playerName The name of the player whose skin is used for the NPC.
     * @param location   The location where the NPC will be spawned.
     */
    public void createNPC(String npcName, String playerName, Location location) {
        UUID npcUUID = UUID.randomUUID();

        this.skinFetcher.fetchSkin(playerName, (skinValue, skinSignature) -> {
            if (skinValue == null && skinSignature == null) return;

            int npcEntityId = ReflectionUtils.generateValidMinecraftEntityId();

            List<PacketPlayerInfoUpdate.Property> properties = new ArrayList<>();
            properties.add(new PacketPlayerInfoUpdate.Property("textures", skinValue, skinSignature));
            NpcProfile npcProfile = new NpcProfile("", npcEntityId, npcUUID, properties);

            SentienceNPC npc = new SentienceNPC(npcEntityId, npcProfile);
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

    /**
     * Creates a new NPC (Non-Player Character) in the game with the specified name, location, and skin properties.
     * The NPC's appearance is determined by the provided skinValue and skinSignature.
     * The newly created NPC is also cached and made visible to all online players. Additionally,
     * the NPC information is saved to a file for persistence.
     *
     * @param npcName         The name of the NPC to be created.
     * @param location        The location where the NPC will be spawned.
     * @param skinValue       The value of the NPC's skin texture.
     * @param skinSignature   The signature for validating the NPC's skin texture.
     */
    public void createNPC(String npcName, Location location, String skinValue, String skinSignature) {
        UUID npcUUID = UUID.randomUUID();

        int npcEntityId = ReflectionUtils.generateValidMinecraftEntityId();

        List<PacketPlayerInfoUpdate.Property> properties = new ArrayList<>();
        properties.add(new PacketPlayerInfoUpdate.Property("textures", skinValue, skinSignature));
        NpcProfile npcProfile = new NpcProfile("", npcEntityId, npcUUID, properties);

        SentienceNPC npc = new SentienceNPC(npcEntityId, npcProfile);
        npc.setLocation(location);

        this.npcCache.put(npcName, npc);
        this.npcIdCache.put(npc.getEntityId(), npcName);
        this.npcIds.add(npc.getEntityId());
        this.saveNPCtoFile(npcName, location, skinValue, skinSignature);
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.spawnNPC(npcName, player);
        }
    }

    /**
     * Creates a new NPC (Non-Player Character) in the game with the specified
     * name, using the skin of the provided player name and spawning it at
     * the specified location. The NPC is saved in an internal cache and made
     * visible to all online players. A callback function is invoked once
     * the NPC creation process is complete or if the operation fails.
     *
     * @param npcName    The name of the NPC to be created.
     * @param playerName The name of the player whose skin will be used for the NPC.
     * @param location   The location where the NPC will be spawned.
     * @param callback   A Runnable that is executed after the NPC is created
     *                   or if the creation process fails. Can be null.
     */
    public void createNPC(String npcName, String playerName, Location location, Runnable callback) {
        UUID npcUUID = UUID.randomUUID();

        this.skinFetcher.fetchSkin(playerName, (skinValue, skinSignature) -> {
            if (skinValue == null && skinSignature == null) {
                if (callback != null) callback.run();
                return;
            }

            int npcEntityId = ReflectionUtils.generateValidMinecraftEntityId();

            List<PacketPlayerInfoUpdate.Property> properties = new ArrayList<>();
            properties.add(new PacketPlayerInfoUpdate.Property("textures", skinValue, skinSignature));
            NpcProfile npcProfile = new NpcProfile("", npcEntityId, npcUUID, properties);

            SentienceNPC npc = new SentienceNPC(npcEntityId, npcProfile);
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

    /**
     * Removes an NPC (Non-Player Character) from the system by its name.
     * This method updates the internal caches and JSON document, and
     * handles cleanup operations, such as despawning NPC instances and
     * removing references.
     *
     * @param npcName The name of the NPC to be removed. If the NPC does not exist, no changes are made.
     */
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

    /**
     * Retrieves a list of all NPC (Non-Player Character) names currently stored in the internal cache.
     *
     * @return a list of strings representing the names of all cached NPCs.
     */
    public List<String> getNPCNames() {
        return new ArrayList<>(this.npcCache.keySet());
    }

    /**
     * Retrieves the number of NPCs (Non-Player Characters) currently loaded
     * in the internal NPC cache.
     *
     * @return the total count of loaded NPCs in the cache.
     */
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
            int npcEntityId = ReflectionUtils.generateValidMinecraftEntityId();

            List<PacketPlayerInfoUpdate.Property> properties = new ArrayList<>();
            properties.add(new PacketPlayerInfoUpdate.Property("textures", skinValue, skinSignature));
            NpcProfile npcProfile = new NpcProfile("", npcEntityId, npcUUID, properties);

            SentienceNPC npc = new SentienceNPC(npcEntityId, npcProfile);
            npc.setLocation(location);

            JsonObject settings = data.getAsJsonObject("settings");
            npc.setShouldLookAtPlayer(settings.get("shouldLookAtPlayer").getAsBoolean());
            npc.setShouldSneakWithPlayer(settings.get("shouldSneakWithPlayer").getAsBoolean());

            this.npcCache.put(npcName, npc);
            this.npcIds.add(npc.getEntityId());
            this.npcIdCache.put(npc.getEntityId(), npcName);
        }
    }

    /**
     * Retrieves an NPC (Non-Player Character) from the internal cache using the given name.
     *
     * @param npcName The name of the NPC to retrieve. Must not be null or empty.
     * @return The SentienceNPC object associated with the specified name, or null if no NPC with the given name is found in the cache.
     */
    public SentienceNPC getNPC(String npcName) {
        return this.npcCache.get(npcName);
    }

    /**
     * Retrieves a set containing all NPCs (Non-Player Characters) currently stored in the internal cache.
     *
     * @return a set of SentienceNPC objects representing all cached NPCs.
     */
    public Set<SentienceNPC> getAllNPCs() {
        Set<SentienceNPC> npcs = new HashSet<>();
        for (Map.Entry<String, SentienceNPC> entry : this.npcCache.entrySet()) {
            npcs.add(entry.getValue());
        }
        return npcs;
    }

    /**
     * Retrieves the name of an NPC (Non-Player Character) using its unique entity ID.
     *
     * @param entityId The unique ID of the NPC whose name is to be retrieved.
     * @return The name of the NPC associated with the given entity ID, or null if no match is found in the cache.
     */
    public String getNpcNameFromId(int entityId) {
        return this.npcIdCache.getOrDefault(entityId, null);
    }

    /**
     * Retrieves the internal map of NPCs (Non-Player Characters) currently stored in the cache.
     *
     * @return a map where the keys are NPC names (as Strings) and the values are
     *         corresponding SentienceNPC objects representing the NPC instances.
     */
    public Map<String, SentienceNPC> getNPCMap() {
        return this.npcCache;
    }

    /**
     * Checks if an NPC (Non-Player Character) with the specified name exists in the internal cache.
     *
     * @param npcName The name of the NPC to check. Must not be null.
     * @return true if an NPC with the given name exists in the cache; false otherwise.
     */
    public boolean doesNPCExist(String npcName) {
        return this.npcCache.containsKey(npcName);
    }

    /**
     * Updates the skin of a specified NPC (Non-Player Character) by fetching the skin
     * using the player's name. If the NPC is marked as persistent, the updated skin
     * information is saved to the internal JSON document file.
     *
     * @param npcName     The name of the NPC whose skin is to be updated. If the NPC does not exist, the method does nothing.
     * @param playerName  The name of the player whose skin will be used to update the NPC. Must not be null.
     * @param persistent  A flag indicating whether the updated skin data should be saved persistently to the file.
     */
    public void updateSkin(String npcName, String playerName, boolean persistent) {

        SentienceNPC npc = this.npcCache.get(npcName);
        if (npc == null) return;

        this.skinFetcher.fetchSkin(playerName, (skinValue, skinSignature) -> {
            if (skinValue == null && skinSignature == null) return;

            npc.changeSkin(skinValue, skinSignature);

            if (persistent) {
                this.jsonDocument = JsonDocument.loadDocument(this.file);
                if (this.jsonDocument == null) return;
                this.jsonDocument.update(npcName + ".skin-value", skinValue);
                this.jsonDocument.update(npcName + ".skin-signature", skinSignature);

                try {
                    this.jsonDocument.save(this.file);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    /**
     * Updates the location of an NPC (Non-Player Character) by teleporting it to the
     * new location and saving the updated location data to a JSON document.
     * If the specified NPC or the JSON document does not exist, the method does nothing.
     *
     * @param npcName  The name of the NPC whose location needs to be updated.
     *                 If the NPC does not exist in the cache, the method does nothing.
     * @param location The new location to which the NPC should be teleported.
     *                 Must not be null and should contain valid coordinates and world information.
     */
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

    /**
     * Toggles the "look at player" behavior for the specified NPC (Non-Player Character).
     * Updates the associated NPC's setting to either enable or disable the behavior
     * and persists the change to the JSON document file.
     *
     * @param npcName The name of the NPC whose "look at player" behavior is being updated.
     *                Must not be null. If the NPC does not exist, the method returns "error".
     * @return A String indicating the result of the update:
     *         - "true" if the "look at player" behavior is enabled after the update.
     *         - "false" if the "look at player" behavior is disabled after the update.
     *         - "error" if the NPC or JSON document does not exist, or if an IOException occurs during save.
     */
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

    /**
     * Toggles the "sneak with player" behavior for the specified NPC (Non-Player Character).
     * Updates the NPC's setting to either enable or disable the behavior and persists
     * the change to the JSON document file.
     *
     * @param npcName The name of the NPC whose "sneak with player" behavior is being updated.
     *                Must not be null. If the NPC does not exist, the method returns "error".
     * @return A String indicating the result of the update:
     *         - "true" if the "sneak with player" behavior is enabled after the update.
     *         - "false" if the "sneak with player" behavior is disabled after the update.
     *         - "error" if the NPC or JSON document does not exist, or if an IOException occurs during save.
     */
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

    /**
     * Spawns an NPC with the specified name near the given player.
     * If the NPC with the provided name is not found in the cache, the method does nothing.
     *
     * @param npcName the name of the NPC to spawn
     * @param player the player near whom the NPC will be spawned
     */
    public void spawnNPC(String npcName, Player player) {
        SentienceNPC npc = this.npcCache.get(npcName);
        if (npc == null) return;

        npc.spawn(player);
    }

    /**
     * Spawns all NPCs in the cache for the specified player.
     * Only spawns NPCs if the provided player is online.
     *
     * @param player the player for whom all NPCs will be spawned; must not be null and must be online
     */
    public void spawnAllNPCs(Player player) {
        if (player == null || !player.isOnline()) return;

        this.npcCache.values().stream()
                .filter(Objects::nonNull)
                .forEach(npc -> npc.spawn(player));
    }

    /**
     * Despawns all NPCs for the given player by iterating through the NPC cache and invoking
     * the despawn method on each NPC instance.
     *
     * @param player The player for whom all NPCs should be despawned. If the player is null
     *               or not online, the method will exit without performing any actions.
     */
    public void despawnAllNPCs(Player player) {
        if (player == null || !player.isOnline()) return;

        this.npcCache.values().stream()
                .filter(Objects::nonNull)
                .forEach(npc -> npc.despawn(player));
    }

    public void despawnAll() {
        this.npcCache.values().forEach(SentienceNPC::despawnAll);
        this.npcCache.clear();
        this.npcIds.clear();
        this.npcIdCache.clear();
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