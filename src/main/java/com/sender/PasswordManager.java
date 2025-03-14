package com.sender;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PasswordManager {
    private static final String CONFIG_FILE = "config.properties";

    public static String getEncryptionPassword() throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            return props.getProperty("encryption.password");
        }
    }
}
