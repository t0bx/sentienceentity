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

package de.t0bx.sentienceEntity.update;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.StreamSupport;

public class UpdateManager {

    private final JavaPlugin plugin;
    private final String red = "\u001B[31m";
    private final String reset = "\u001B[0m";
    private final String green = "\u001B[32m";
    private final String yellow = "\u001B[33m";

    public UpdateManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                String url = "https://repository.t0bx.de/service/rest/v1/search?repository=spigotmc-releases";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Accept", "application/json")
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                String json = response.body();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(json);

                JsonNode items = root.get("items");
                if (!items.isArray() || items.isEmpty()) {
                    consumer.accept("Unknown");
                    return;
                }

                List<String> versions = StreamSupport.stream(items.spliterator(), false)
                        .map(item -> item.path("version").asText())
                        .toList();

                String latest = versions.get(versions.size() - 1);

                consumer.accept(latest);
            } catch (IOException | InterruptedException exception) {
                this.plugin.getLogger().log(Level.WARNING, "Error while checking for updates!", exception);
            }
        });
    }

    public void checkForUpdate() {
        this.plugin.getLogger().info("Checking for updates...");
        this.getVersion(version -> {
            String currentVersion = this.plugin.getDescription().getVersion();
            int currentVersionId = Integer.parseInt(currentVersion.replace(".", ""));
            int versionId = Integer.parseInt(version.replace(".", ""));

            if (currentVersionId == versionId) {
                this.plugin.getLogger().info(green + "SentienceEntity is up to date!" + reset);
            } else if (currentVersionId < versionId) {
                this.plugin.getLogger().info(yellow + "SentienceEntity got a update (" + currentVersion + " >> " + version + ")!" + reset);
                this.plugin.getLogger().info(yellow + "Download >> https://www.spigotmc.org/resources/124834/" + reset);
            } else {
                this.plugin.getLogger().info(red + "You are running an unsupported version of SentienceEntity, note that some features may not work correctly!" + reset);
            }
        });
    }
}
