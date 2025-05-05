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
