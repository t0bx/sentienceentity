package de.t0bx.sentienceEntity.config;

import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.utils.JsonDocument;

import java.io.File;
import java.io.IOException;

public class ConfigFileManager {

    private final File configFile;
    private JsonDocument jsonDocument;

    public ConfigFileManager() {
        this.configFile = new File(SentienceEntity.getInstance().getDataFolder(), "config.json");
        this.loadConfig();
    }

    private void loadConfig() {
        try {
            this.jsonDocument = JsonDocument.loadDocument(this.configFile);

            if (this.jsonDocument == null) {
                this.jsonDocument = new JsonDocument();

                this.jsonDocument.setBoolean("bStats", true);

                this.jsonDocument.save(this.configFile);
            }

            SentienceEntity.getInstance().setBStatsEnabled(this.jsonDocument.get("bStats").getAsBoolean());
        } catch (IOException exception) {
            SentienceEntity.getInstance().getLogger().warning("Failed to load config file: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
