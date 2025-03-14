package com.sender;

import com.google.gson.*;

import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

public class ConfigBootstrapper {
    private static final String CONFIG_FILE = "battery_config.json";
    private static final String PROPERTIES_FILE = "config.properties";

    public static void ensureConfigFilesExist() throws IOException {
        generateBatteryConfigIfMissing();
        generatePropertiesIfMissing();
    }

    private static void generateBatteryConfigIfMissing() throws IOException {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            JsonObject config = new JsonObject();
            config.addProperty("cellCount", 4); // default 4 cells
            config.addProperty("encryptionSalt", "FixedSaltForDemo");

            JsonArray fields = new JsonArray();

            fields.add(createField("voltage", "V", 3.2, 4.2, 0.05));
            fields.add(createField("current", "A", -10.0, 10.0, 0.5));
            fields.add(createField("temperature", "C", 20.0, 60.0, 1.0));

            config.add("fields", fields);

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(config, writer);
            }

            System.out.println("Generated default battery_config.json");
        }
    }

    private static JsonObject createField(String key, String unit, double min, double max, double step) {
        JsonObject field = new JsonObject();
        field.addProperty("key", key);
        field.addProperty("unit", unit);
        field.addProperty("min", min);
        field.addProperty("max", max);
        field.addProperty("step", step);
        return field;
    }

    private static void generatePropertiesIfMissing() throws IOException {
        File propFile = new File(PROPERTIES_FILE);
        if (!propFile.exists()) {
            Properties props = new Properties();
            props.setProperty("encryption.password", "MySecurePassword123");

            try (FileWriter writer = new FileWriter(propFile)) {
                props.store(writer, "Auto-generated configuration");
            }

            System.out.println("Generated default config.properties");
        }
    }
}
