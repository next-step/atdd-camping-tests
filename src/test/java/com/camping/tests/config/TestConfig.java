package com.camping.tests.config;

public class TestConfig {

    public static final String KIOSK_BASE_URL = getEnvOrDefault("KIOSK_BASE_URL", "http://localhost:18081");
    public static final String ADMIN_BASE_URL = getEnvOrDefault("ADMIN_BASE_URL", "http://localhost:18082");
    public static final String RESERVATION_BASE_URL = getEnvOrDefault("RESERVATION_BASE_URL", "http://localhost:18083");

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }
}
