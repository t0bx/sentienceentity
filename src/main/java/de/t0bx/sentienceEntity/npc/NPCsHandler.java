package de.t0bx.sentienceEntity.npc;

import com.github.retrooper.packetevents.protocol.npc.NPC;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.utils.JsonDocument;
import de.t0bx.sentienceEntity.utils.SentienceLocation;
import de.t0bx.sentienceEntity.utils.SkinFetcher;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NPCsHandler {

    private final Map<String, SentienceNPC> npcCache;
    private JsonDocument jsonDocument;
    private final SkinFetcher skinFetcher;
    private final File file;
    private final HologramManager hologramManager;

    public NPCsHandler() {
        this.npcCache = new HashMap<>();
        this.skinFetcher = SentienceEntity.getInstance().getSkinFetcher();
        this.file = new File(SentienceEntity.getInstance().getDataFolder(), "npcs.json");
        this.hologramManager = SentienceEntity.getInstance().getHologramManager();
        this.loadNPCsFromFile();
    }

    public void createNPC(String npcName, String playerName, Location location) {
        UUID npcUUID = UUID.randomUUID();

        this.skinFetcher.fetchSkin(playerName, (skinValue, skinSignature) -> {
            if (skinValue == null && skinSignature == null) return;

            UserProfile userProfile = new UserProfile(npcUUID, "");
            userProfile.setTextureProperties(Collections.singletonList(new TextureProperty("textures", skinValue, skinSignature)));

            SentienceNPC npc = new SentienceNPC(SpigotReflectionUtil.generateEntityId(), userProfile);
            npc.setLocation(SentienceLocation.fromBukkitLocation(location));
            this.hologramManager.createHologram(npcName, location, List.of("<green>First Line", "<red>Second Line"));

            this.npcCache.put(npcName, npc);
            this.saveNPCtoFile(npcName, location, skinValue, skinSignature);
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.spawnNPC(npcName, player);
            }
        });
    }

    public void removeNPC(String npcName) {
        this.jsonDocument = JsonDocument.loadDocument(this.file);
        if (this.jsonDocument == null) return;
        if (!this.npcCache.containsKey(npcName)) return;

        SentienceNPC npc = this.npcCache.remove(npcName);
        npc.despawnAll();

        this.jsonDocument.remove(npcName);

        try {
            this.jsonDocument.save(this.file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
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
            //List<String> lines = this.hologramManager.loadLinesFromFile(npcName);
            //this.hologramManager.createHologram(npcName, location, lines);

            String skinValue = data.get("skin-value").getAsString();
            String skinSignature = data.get("skin-signature").getAsString();

            UUID npcUUID = UUID.randomUUID();
            UserProfile profile = new UserProfile(npcUUID, "");
            profile.setTextureProperties(Collections.singletonList(
                    new TextureProperty("textures", skinValue, skinSignature)
            ));

            SentienceNPC npc = new SentienceNPC(SpigotReflectionUtil.generateEntityId(), profile);
            npc.setLocation(SentienceLocation.fromBukkitLocation(location));

            JsonObject settings = data.getAsJsonObject("settings");
            npc.setShouldLookAtPlayer(settings.get("shouldLookAtPlayer").getAsBoolean());
            npc.setShouldSneakWithPlayer(settings.get("shouldSneakWithPlayer").getAsBoolean());

            this.npcCache.put(npcName, npc);
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

        npc.teleport(SentienceLocation.fromBukkitLocation(location));

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
        for (SentienceNPC npc : this.npcCache.values()) {
            if (npc == null) continue;

            npc.spawn(player);
        }
    }

    public void despawnAllNPCs(Player player) {
        for (SentienceNPC npc : this.npcCache.values()) {
            if (npc == null) continue;

            npc.despawn(player);
        }
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