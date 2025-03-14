package com.sender;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigWatcher implements Runnable {
    private final String configFilePath;
    private final AtomicReference<JsonObject> configRef;

    public ConfigWatcher(String configFilePath, AtomicReference<JsonObject> configRef) {
        this.configFilePath = configFilePath;
        this.configRef = configRef;
    }

    @Override
    public void run() {
        try {
            Path configPath = Paths.get(configFilePath).toAbsolutePath();
            Path dir = configPath.getParent();

            WatchService watcher = FileSystems.getDefault().newWatchService();
            dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                WatchKey key = watcher.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        Path changed = (Path) event.context();
                        if (changed.endsWith(configPath.getFileName())) {
                            try {
                                Thread.sleep(500); // debounce delay
                                JsonObject updated = ConfigManager.loadConfig();
                                configRef.set(updated);
                                System.out.println("Config updated from file.");
                            } catch (Exception e) {
                                System.err.println("Failed to reload config: " + e.getMessage());
                            }
                        }
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
