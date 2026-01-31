package com.camping.tests.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

public class ExternalAPIConfig {

    private static final Map<String, String> applicationHost = Map.of(
            "어드민시스템", adminHost(),
            "키오스크시스템", kioskHost(),
            "예약시스템", reservationHost()
    );

    public static String getServiceHost(String serviceName) {
        return applicationHost.get(serviceName);
    }

    public static String adminHost() {
        try {
            Properties props = new Properties();
            props.load(Files.newInputStream(Paths.get("src/test/resources/test-config.yml")));
            return props.getProperty("admin-url").trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test-config.yml", e);
        }
    }

    public static String reservationHost() {
        try {
            Properties props = new Properties();
            props.load(Files.newInputStream(Paths.get("src/test/resources/test-config.yml")));
            return props.getProperty("reservation-url").trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test-config.yml", e);
        }
    }

    public static String kioskHost() {
        try {
            Properties props = new Properties();
            props.load(Files.newInputStream(Paths.get("src/test/resources/test-config.yml")));
            return props.getProperty("kiosk-url").trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test-config.yml", e);
        }
    }

    public static String getAdminUsername() {
        try {
            Properties props = new Properties();
            props.load(Files.newInputStream(Paths.get("src/test/resources/test-config.yml")));
            return props.getProperty("admin-username").trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test-config.yml", e);
        }
    }

    public static String getAdminPassword() {
        try {
            Properties props = new Properties();
            props.load(Files.newInputStream(Paths.get("src/test/resources/test-config.yml")));
            return props.getProperty("admin-password").trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test-config.yml", e);
        }
    }
}
