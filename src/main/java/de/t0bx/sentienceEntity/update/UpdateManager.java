package de.t0bx.sentienceEntity.update;

import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateManager {

    private final JavaPlugin plugin;

    public UpdateManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
           try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=124834").openStream()) {
               Scanner scanner = new Scanner(inputStream);
               if (scanner.hasNext()) {
                   consumer.accept(scanner.next());
               }
           } catch (IOException exception) {
               this.plugin.getLogger().warning("Failed to load update information: " + exception.getMessage());
           }
        });
    }

    public void checkForUpdate() {
        this.plugin.getLogger().info("Checking for updates...");
        this.getVersion(version -> {
            String currentVersion = this.plugin.getDescription().getVersion();
            if (!currentVersion.equals(version)) {
                this.plugin.getLogger().info("SentienceEntity got a update (" + currentVersion + " >> " + version + ")!");
                this.plugin.getLogger().info("Download >> https://www.spigotmc.org/resources/124834/");
            } else {
                this.plugin.getLogger().info("SentienceEntity is up to date!");
            }
        });
    }
}
