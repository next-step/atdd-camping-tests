package com.camping.tests.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@SuppressWarnings("NonAsciiCharacters")
public class ExternalAPIConfig {

    public static String 키오스크_시스템_호스트() {
        try {
            Properties props = new Properties();
            props.load(Files.newInputStream(Paths.get("src/test/resources/test-config.yml")));
            return props.getProperty("kiosk-url").trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test-config.yml", e);
        }
    }
}
