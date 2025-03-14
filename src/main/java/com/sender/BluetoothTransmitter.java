package com.sender;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.microedition.io.StreamConnectionNotifier;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.*;
import java.util.concurrent.atomic.AtomicReference;

public class BluetoothTransmitter {

    private final StreamConnectionNotifier notifier;
    private final DataEncryptor decryptor;
    private final AtomicReference<JsonObject> configRef;

    public BluetoothTransmitter(DataEncryptor decryptor, AtomicReference<JsonObject> configRef) throws IOException {
        String uuid = "btspp://localhost:0000110100001000800000805F9B34FB;name=BatterySender";
        this.notifier = (StreamConnectionNotifier) Connector.open(uuid);
        this.decryptor = decryptor;
        this.configRef = configRef;
    }

    public void listen() {
        new Thread(() -> {
            while (true) {
                StreamConnection conn = null;
                try {
                    conn = notifier.acceptAndOpen();
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.openInputStream()));

                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.startsWith("CONFIG_UPDATE:")) {
                            String encryptedJson = line.substring("CONFIG_UPDATE:".length());
                            String jsonStr = decryptor.decrypt(encryptedJson);
                            JsonObject newConfig = JsonParser.parseString(jsonStr).getAsJsonObject();

                            ConfigManager.saveConfig(newConfig);
                            configRef.set(newConfig);
                            System.out.println("🔁 Config updated remotely!");

                        } else {
                            System.out.println("Received (unknown): " + line);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                } finally {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }
}
