package com.camping.tests.support;

public class TestConfig {

    public static final String KIOSK_BASE_URL =
            System.getenv().getOrDefault("KIOSK_BASE_URL", "http://localhost:8080");

    private TestConfig() {}
}
