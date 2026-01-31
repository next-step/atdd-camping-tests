package com.camping.tests.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public final class TestEnvironment {

    private static final Properties PROPS = new Properties();

    static {
        try {
            PROPS.load(
                    Files.newInputStream(Paths.get("src/test/resources/test-config.yml"))
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test-config.yml", e);
        }
    }

    private TestEnvironment() {
        // util class
    }

    private static String get(String key) {
        String value = PROPS.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value.trim();
    }

    public static String adminHost() {
        return get("admin-url");
    }

    public static String reservationHost() {
        return get("reservation-url");
    }

    public static String kioskHost() {
        return get("kiosk-url");
    }

    public static String adminUsername() {
        return get("admin-username");
    }

    public static String adminPassword() {
        return get("admin-password");
    }
}

