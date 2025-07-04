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

    /**
     * Represents the data associated with a player's Minecraft skin, including the texture value
     * and the corresponding signature for validation.
     */
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
