package com.sender;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;

public class ConfigManager {
    private static final String CONFIG_PATH = "src/main/resources/battery_config.json";

    public static JsonObject loadConfig() throws IOException {
        String json = new String(Files.readAllBytes(Paths.get(CONFIG_PATH)));
        return JsonParser.parseString(json).getAsJsonObject();
    }

    public static void saveConfig(JsonObject newConfig) throws IOException {
        Files.write(Paths.get(CONFIG_PATH), new GsonBuilder().setPrettyPrinting().create().toJson(newConfig).getBytes());
    }
}
