package com.camping.tests.support;

public class TestConfig {

    public static final String KIOSK_BASE_URL =
            System.getenv().getOrDefault("KIOSK_BASE_URL", "http://localhost:8080");

    public static final String ADMIN_BASE_URL =
            System.getenv().getOrDefault("ADMIN_BASE_URL", "http://localhost:8081");

    public static final String RESERVATION_BASE_URL =
            System.getenv().getOrDefault("RESERVATION_BASE_URL", "http://localhost:8082");

    private TestConfig() {}
}
