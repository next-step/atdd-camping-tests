package com.camping.tests.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = TestConfig.class.getClassLoader()
                .getResourceAsStream("test.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test.properties", e);
        }
    }

    public static String get(String key) {
        String envKey = key.toUpperCase().replace(".", "_").replace("-", "_");
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        String sysValue = System.getProperty(key);
        if (sysValue != null && !sysValue.isEmpty()) {
            return sysValue;
        }
        return properties.getProperty(key);
    }

    // Base URLs
    public static String getAdminBaseUrl() {
        return get("admin.base-url");
    }

    public static String getKioskBaseUrl() {
        return get("kiosk.base-url");
    }

    public static String getReservationBaseUrl() {
        return get("reservation.base-url");
    }

    // Database
    public static String getDbUrl() {
        return get("db.url");
    }

    public static String getDbUsername() {
        return get("db.username");
    }

    public static String getDbPassword() {
        return get("db.password");
    }
}