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

package de.t0bx.sentienceEntity.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public class SkinFetcher {
    private final JavaPlugin plugin;
    private final Map<String, SkinData> skinCache = new HashMap<>();

    public SkinFetcher(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Fetches the skin data for a given Minecraft player.
     * If the skin data for the player is cached, it will be retrieved immediately.
     * Otherwise, the data is fetched asynchronously from Mojang's API and then cached.
     * The provided callback will be invoked with the skin value and signature upon completion.
     * If an error occurs or the player is not found, the callback will be invoked with null values.
     *
     * @param playerName The name of the Minecraft player whose skin data is to be fetched.
     * @param callback   A BiConsumer that accepts two strings: the skin value and the skin signature.
     *                   If the data fetch fails, both parameters will be null.
     */
    public void fetchSkin(String playerName, BiConsumer<String, String> callback) {
        if (skinCache.containsKey(playerName.toLowerCase())) {
            SkinData cachedSkin = skinCache.get(playerName.toLowerCase());
            callback.accept(cachedSkin.getValue(), cachedSkin.getSignature());
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String uuidUrl = "https://api.mojang.com/users/profiles/minecraft/" + playerName;
                String uuidResponse = makeHttpRequest(uuidUrl);

                if (uuidResponse == null || uuidResponse.isEmpty()) {
                    syncCallback(callback, null, null);
                    return;
                }

                JsonObject uuidObject = JsonParser.parseString(uuidResponse).getAsJsonObject();
                String uuidStr = uuidObject.get("id").getAsString();

                UUID uuid = UUID.fromString(
                        uuidStr.replaceFirst(
                                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                "$1-$2-$3-$4-$5"
                        )
                );

                String profileUrl = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", "") + "?unsigned=false";
                String profileResponse = makeHttpRequest(profileUrl);

                if (profileResponse == null || profileResponse.isEmpty()) {
                    syncCallback(callback, null, null);
                    return;
                }

                JsonObject profileObject = JsonParser.parseString(profileResponse).getAsJsonObject();
                JsonObject texturesProperty = profileObject
                        .getAsJsonArray("properties")
                        .get(0)
                        .getAsJsonObject();

                String skinValue = texturesProperty.get("value").getAsString();
                String skinSignature = texturesProperty.get("signature").getAsString();

                skinCache.put(playerName.toLowerCase(), new SkinData(skinValue, skinSignature));

                syncCallback(callback, skinValue, skinSignature);

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to fetch Skin for " + playerName, e);
                syncCallback(callback, null, null);
            }
        });
    }

    private void syncCallback(BiConsumer<String, String> callback, String value, String signature) {
        Bukkit.getScheduler().runTask(plugin, () -> callback.accept(value, signature));
    }

    private String makeHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            return response.toString();
        }

        return null;
    }

    public class SkinData {
        private final String value;
        private final String signature;

        public SkinData(String value, String signature) {
            this.value = value;
            this.signature = signature;
        }

        public String getValue() {
            return value;
        }

        public String getSignature() {
            return signature;
        }
    }
}
