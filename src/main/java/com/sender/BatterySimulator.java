package com.sender;

import com.google.gson.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class BatterySimulator {
    public static void main(String[] args) {
        try {
            ConfigBootstrapper.ensureConfigFilesExist();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            // Load initial config
            AtomicReference<JsonObject> configRef = new AtomicReference<>(ConfigManager.loadConfig());
            // Start background thread to monitor file changes
            new Thread(new ConfigWatcher("battery_config.json", configRef)).start();

            JsonObject configJson = configRef.get();

            // Use for encryption
            BatteryConfig initialConfig = new Gson().fromJson(configJson, BatteryConfig.class);
            String salt = initialConfig.getEncryptionSalt();
            String password = PasswordManager.getEncryptionPassword();

            DataEncryptor encryptor = new DataEncryptor(password, salt);
            DataEncryptor decryptor = new DataEncryptor(password, salt);

            BluetoothTransmitter transmitter = new BluetoothTransmitter(decryptor, configRef);
            transmitter.listen(); // Start listening for remote config updates

            // Delay to allow Bluetooth to establish
            Thread.sleep(3000);

            while (true) {
                JsonObject dynamicConfig = configRef.get();
                int cellCount = dynamicConfig.get("cellCount").getAsInt();
                JsonArray fields = dynamicConfig.getAsJsonArray("fields");

                // Simulated state storage
                Map<String, Double> state = new HashMap<>();
                Map<String, Boolean> direction = new HashMap<>();

                for (JsonElement el : fields) {
                    JsonObject f = el.getAsJsonObject();
                    String key = f.get("key").getAsString();
                    double min = f.get("min").getAsDouble();
                    double max = f.get("max").getAsDouble();
                    state.put(key, min);
                    direction.put(key, true); // true = increasing
                }

                // Send metadata once
                String meta = new MetadataGenerator().generateMetadataJson(dynamicConfig);
                String encryptedMeta = encryptor.encrypt(meta);
                BluetoothSender.send(encryptedMeta);

                while (true) {
                    dynamicConfig = configRef.get(); // Refresh config live
                    fields = dynamicConfig.getAsJsonArray("fields");
                    cellCount = dynamicConfig.get("cellCount").getAsInt();

                    for (int cell = 1; cell <= cellCount; cell++) {
                        StringBuilder payload = new StringBuilder("BATT_" + cell);
                        for (JsonElement el : fields) {
                            JsonObject f = el.getAsJsonObject();
                            String key = f.get("key").getAsString();
                            double min = f.get("min").getAsDouble();
                            double max = f.get("max").getAsDouble();
                            double step = f.get("step").getAsDouble();

                            double val = state.get(key);
                            boolean dir = direction.get(key);

                            // Update sequentially
                            val += dir ? step : -step;
                            if (val > max) {
                                val = max;
                                direction.put(key, false);
                            } else if (val < min) {
                                val = min;
                                direction.put(key, true);
                            }

                            state.put(key, val);
                            payload.append(",").append(key).append(":").append(String.format("%.2f", val));
                        }

                        String encrypted = encryptor.encrypt(payload.toString());
                        BluetoothSender.send(encrypted);
                        Thread.sleep(200); // Delay between each cell
                    }

                    // Mark end of batch
                    BluetoothSender.send(encryptor.encrypt("END"));
                    Thread.sleep(1000);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
