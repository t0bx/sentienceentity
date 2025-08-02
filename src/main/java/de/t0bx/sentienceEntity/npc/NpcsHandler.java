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

package de.t0bx.sentienceEntity.npc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.t0bx.sentienceEntity.SentienceAPI;
import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.hologram.SentienceHologram;
import de.t0bx.sentienceEntity.network.inventory.equipment.EquipmentSlot;
import de.t0bx.sentienceEntity.network.utils.NpcProfile;
import de.t0bx.sentienceEntity.network.wrapper.packets.PacketPlayerInfoUpdate;
import de.t0bx.sentienceEntity.utils.JsonDocument;
import de.t0bx.sentienceEntity.utils.ReflectionUtils;
import de.t0bx.sentienceEntity.utils.SkinFetcher;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

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
    public void createNPC(String npcName, String playerName, EntityType entityType, Location location, boolean persistent) {
        UUID npcUUID = UUID.randomUUID();

        this.skinFetcher.fetchSkin(playerName, (skinValue, skinSignature) -> {
            if (skinValue == null && skinSignature == null) return;

            int npcEntityId = ReflectionUtils.generateValidMinecraftEntityId();

            List<PacketPlayerInfoUpdate.Property> properties = new ArrayList<>();
            properties.add(new PacketPlayerInfoUpdate.Property("textures", skinValue, skinSignature));
            NpcProfile npcProfile = new NpcProfile("", npcEntityId, npcUUID);
            npcProfile.setProperties(properties);

            SentienceNPC npc = new SentienceNPC(npcName, npcEntityId, entityType, npcProfile);
            npc.setLocation(location);

            this.npcCache.put(npcName, npc);
            this.npcIdCache.put(npc.getEntityId(), npcName);
            this.npcIds.add(npc.getEntityId());

            if (persistent) {
                this.saveNPCtoFile(npc);
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
    public void createNPC(String npcName, EntityType entityType, @Nullable String playerName, Location location, @Nullable String permission) {
        UUID npcUUID = UUID.randomUUID();
        int npcEntityId = ReflectionUtils.generateValidMinecraftEntityId();

        NpcProfile npcProfile = new NpcProfile("", npcEntityId, npcUUID);

        SentienceNPC npc = new SentienceNPC(npcName, npcEntityId, entityType, npcProfile);
        npc.setLocation(location);
        if (permission != null) npc.setPermission(permission);

        if (entityType == EntityType.PLAYER && playerName != null) {
            this.skinFetcher.fetchSkin(playerName, (skinValue, skinSignature) -> {
                if (skinValue == null && skinSignature == null) return;

                List<PacketPlayerInfoUpdate.Property> properties = new ArrayList<>();
                properties.add(new PacketPlayerInfoUpdate.Property("textures", skinValue, skinSignature));
                npcProfile.setProperties(properties);

                this.npcCache.put(npcName, npc);
                this.npcIdCache.put(npc.getEntityId(), npcName);
                this.npcIds.add(npc.getEntityId());

                this.saveNPCtoFile(npc);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    this.spawnNPC(npcName, player);
                }
            });
            return;
        }

        this.npcCache.put(npcName, npc);
        this.npcIdCache.put(npc.getEntityId(), npcName);
        this.npcIds.add(npc.getEntityId());

        this.saveNPCtoFile(npc);
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.spawnNPC(npcName, player);
        }
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
    public void createPlayerNpc(String npcName, Location location, String skinValue, String skinSignature) {
        UUID npcUUID = UUID.randomUUID();

        int npcEntityId = ReflectionUtils.generateValidMinecraftEntityId();

        List<PacketPlayerInfoUpdate.Property> properties = new ArrayList<>();
        properties.add(new PacketPlayerInfoUpdate.Property("textures", skinValue, skinSignature));
        NpcProfile npcProfile = new NpcProfile("", npcEntityId, npcUUID);
        npcProfile.setProperties(properties);

        SentienceNPC npc = new SentienceNPC(npcName, npcEntityId, EntityType.PLAYER, npcProfile);
        npc.setLocation(location);

        this.npcCache.put(npcName, npc);
        this.npcIdCache.put(npc.getEntityId(), npcName);
        this.npcIds.add(npc.getEntityId());
        this.saveNPCtoFile(npc);
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
    public void createNPC(String npcName, String playerName, EntityType entityType, Location location, Runnable callback) {
        UUID npcUUID = UUID.randomUUID();

        this.skinFetcher.fetchSkin(playerName, (skinValue, skinSignature) -> {
            if (skinValue == null && skinSignature == null) {
                if (callback != null) callback.run();
                return;
            }

            int npcEntityId = ReflectionUtils.generateValidMinecraftEntityId();

            List<PacketPlayerInfoUpdate.Property> properties = new ArrayList<>();
            properties.add(new PacketPlayerInfoUpdate.Property("textures", skinValue, skinSignature));
            NpcProfile npcProfile = new NpcProfile("", npcEntityId, npcUUID);
            npcProfile.setProperties(properties);

            SentienceNPC npc = new SentienceNPC(npcName, npcEntityId, entityType, npcProfile);
            npc.setLocation(location);

            this.npcCache.put(npcName, npc);
            this.npcIdCache.put(npc.getEntityId(), npcName);
            this.npcIds.add(npc.getEntityId());
            this.saveNPCtoFile(npc);

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
            SentienceEntity.getInstance().getLogger().log(Level.SEVERE, "Failed to save Npc File", exception);
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

            EntityType entityType = EntityType.valueOf(data.get("type").getAsString().toUpperCase());

            double x = data.get("location-x").getAsDouble();
            double y = data.get("location-y").getAsDouble();
            double z = data.get("location-z").getAsDouble();
            float yaw = data.get("location-yaw").getAsFloat();
            float pitch = data.get("location-pitch").getAsFloat();
            String worldName = data.get("location-world").getAsString();

            if (Bukkit.getWorld(worldName) == null) continue;

            Location location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);

            UUID npcUUID = UUID.randomUUID();
            int npcEntityId = ReflectionUtils.generateValidMinecraftEntityId();

            NpcProfile npcProfile = new NpcProfile("", npcEntityId, npcUUID);

            if (entityType == EntityType.PLAYER) {
                String skinValue = data.get("skin-value").getAsString();
                String skinSignature = data.get("skin-signature").getAsString();

                List<PacketPlayerInfoUpdate.Property> properties = new ArrayList<>();
                properties.add(new PacketPlayerInfoUpdate.Property("textures", skinValue, skinSignature));
                npcProfile.setProperties(properties);
            }

            SentienceNPC npc = new SentienceNPC(npcName, npcEntityId, entityType, npcProfile);
            npc.setLocation(location);

            JsonObject settings = data.getAsJsonObject("settings");
            npc.setShouldLookAtPlayer(settings.get("shouldLookAtPlayer").getAsBoolean());
            npc.setShouldSneakWithPlayer(settings.get("shouldSneakWithPlayer").getAsBoolean());

            String permission = settings.get("permission").getAsString().equalsIgnoreCase("none") ? null : settings.get("permission").getAsString();
            npc.setPermission(permission);

            if (settings.has("equipment")) {
                JsonObject equipment = settings.getAsJsonObject("equipment");
                for (Map.Entry<String, JsonElement> equipmentEntry : equipment.entrySet()) {
                    EquipmentSlot equipmentSlot = EquipmentSlot.valueOf(equipmentEntry.getKey().toUpperCase());
                    Material material = Material.valueOf(equipmentEntry.getValue().getAsString());
                    if (material == Material.AIR) continue;

                    npc.addEquipment(equipmentSlot, new ItemStack(material));
                }
            }

            if (data.has("path")) {
                String path = data.get("path").getAsString();
                npc.setBoundedPathName(path);
            }

            this.npcCache.put(npcName, npc);
            this.npcIds.add(npc.getEntityId());
            this.npcIdCache.put(npc.getEntityId(), npcName);
        }
    }

    /**
     * Sets the path for a specified NPC (Non-Player Character) and updates the corresponding
     * JSON document to persist the changes.
     *
     * @param npcName the name of the NPC whose path is being set
     * @param path the new path to assign to the NPC
     */
    public void setPath(String npcName, String path) {
        SentienceNPC npc = this.npcCache.get(npcName);
        if (npc == null) return;
        npc.setBoundedPathName(path);

        this.jsonDocument = JsonDocument.loadDocument(this.file);
        if (this.jsonDocument == null) return;

        this.jsonDocument.update(npcName + ".path", path);

        try {
            this.jsonDocument.save(this.file);
        } catch (IOException exception) {
            SentienceEntity.getInstance().getLogger().log(Level.SEVERE, "Failed to save Npc File", exception);
        }
    }

    /**
     * Retrieves an NPC (Non-Player Character) by name from the NPC cache.
     *
     * @param npcName the name of the NPC to retrieve
     * @return the NPC object associated with the given name, or null if no NPC is found
     */
    public SentienceNPC getNPC(String npcName) {
        return this.npcCache.getOrDefault(npcName, null);
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
                    SentienceEntity.getInstance().getLogger().log(Level.SEVERE, "Failed to save Npc File", exception);
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

        SentienceHologram hologram = SentienceEntity.getApi().getHologramManager().getHologram(npcName);
        if (hologram != null) {
            hologram.updateLocation(location);
        }

        this.jsonDocument.update(npcName + ".location-x", location.getX());
        this.jsonDocument.update(npcName + ".location-y", location.getY());
        this.jsonDocument.update(npcName + ".location-z", location.getZ());
        this.jsonDocument.update(npcName + ".location-yaw", location.getYaw());
        this.jsonDocument.update(npcName + ".location-pitch", location.getPitch());
        this.jsonDocument.update(npcName + ".location-world", location.getWorld().getName());

        try {
            this.jsonDocument.save(this.file);
        } catch (IOException exception) {
            SentienceEntity.getInstance().getLogger().log(Level.SEVERE, "Failed to save Npc File", exception);
        }
    }

    /**
     * Updates the equipment of a specific NPC (Non-Player Character) in the system.
     *
     * @param npcName       the name of the NPC whose equipment is to be updated
     * @param equipmentSlot the equipment slot to be updated
     * @param itemStack     the item to equip in the specified slot; if null, the slot will be cleared
     */
    public void updateEquipment(String npcName, EquipmentSlot equipmentSlot, @Nullable ItemStack itemStack) {
        this.jsonDocument = JsonDocument.loadDocument(this.file);
        SentienceNPC npc = this.npcCache.get(npcName);
        if (this.jsonDocument == null) return;
        if (npc == null) return;

        if (itemStack != null) {
            npc.addEquipment(equipmentSlot, itemStack);

            this.jsonDocument.update(npcName + ".settings.equipment." + equipmentSlot.name().toLowerCase(), itemStack.getType().name().toUpperCase());
        } else {
            npc.removeEquipment(equipmentSlot);

            this.jsonDocument.update(npcName + ".settings.equipment." + equipmentSlot.name().toLowerCase(), Material.AIR.name().toUpperCase());
        }

        try {
            this.jsonDocument.save(this.file);
        } catch (IOException exception) {
            SentienceEntity.getInstance().getLogger().log(Level.SEVERE, "Failed to save Npc File", exception);
        }
    }

    public void updatePermission(String npcName, @Nullable String permission) {
        this.jsonDocument = JsonDocument.loadDocument(this.file);
        SentienceNPC npc = this.npcCache.get(npcName);
        if (this.jsonDocument == null) return;
        if (npc == null) return;

        npc.setPermission(permission);

        this.jsonDocument.update(npcName + ".settings.permission", permission == null ? "none" : permission);

        try {
            this.jsonDocument.save(this.file);
        } catch (IOException exception) {
            SentienceEntity.getInstance().getLogger().log(Level.SEVERE, "Failed to save Npc File", exception);
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
            SentienceEntity.getInstance().getLogger().log(Level.SEVERE, "Failed to save Npc File", exception);
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
            SentienceEntity.getInstance().getLogger().log(Level.SEVERE, "Failed to save Npc File", exception);
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

    private void saveNPCtoFile(SentienceNPC npc) {
        try {
            this.jsonDocument = JsonDocument.loadDocument(this.file);

            if (this.jsonDocument == null) {
                this.jsonDocument = new JsonDocument();
            }

            JsonObject existingData = this.jsonDocument.getJsonObject();
            if (existingData == null) {
                existingData = new JsonObject();
            }

            JsonObject npcObject = this.getJsonObject(npc);
            existingData.add(npc.getName(), npcObject);

            this.jsonDocument.setJsonObject(existingData);
            this.jsonDocument.save(this.file);
        } catch (IOException exception) {
            SentienceEntity.getInstance().getLogger().log(Level.SEVERE, "Failed to save Npc File", exception);
        }
    }

    private @NotNull JsonObject getJsonObject(SentienceNPC npc) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", npc.getEntityType().name());

        jsonObject.addProperty("location-x", npc.getLocation().getX());
        jsonObject.addProperty("location-y", npc.getLocation().getY());
        jsonObject.addProperty("location-z", npc.getLocation().getZ());
        jsonObject.addProperty("location-yaw", npc.getLocation().getYaw());
        jsonObject.addProperty("location-pitch", npc.getLocation().getPitch());
        jsonObject.addProperty("location-world", npc.getLocation().getWorld().getName());

        if (npc.getEntityType() == EntityType.PLAYER && npc.getProfile().getProperties() != null && !npc.getProfile().getProperties().isEmpty()) {
            PacketPlayerInfoUpdate.Property property = npc.getProfile().getProperties().get(0);
            jsonObject.addProperty("skin-value", property.value());
            jsonObject.addProperty("skin-signature", property.signature());
        }

        JsonObject settingsObject = new JsonObject();
        settingsObject.addProperty("shouldLookAtPlayer", false);
        settingsObject.addProperty("shouldSneakWithPlayer", false);
        settingsObject.addProperty("permission", npc.getPermission() == null ? "none" : npc.getPermission());
        jsonObject.add("settings", settingsObject);
        return jsonObject;
    }
}