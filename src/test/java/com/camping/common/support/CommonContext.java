package com.camping.common.support;

import io.restassured.response.Response;

import java.util.Map;

public final class CommonContext {

    public static final String KIOSK_BASE_URL_PROPERTY_KEY = "KIOSK_BASE_URL";
    public static final String KIOSK_DEFAULT_URL = "http://localhost:18080";
    public static String KIOSK_BASE_URL;

    public static final String ADMIN_BASE_URL_PROPERTY_KEY = "ADMIN_BASE_URL";
    public static final String ADMIN_DEFAULT_URL = "http://localhost:18081";
    public static String ADMIN_BASE_URL;

    public static final String RESERVATION_BASE_URL_PROPERTY_KEY = "RESERVATION_BASE_URL";
    public static final String RESERVATION_DEFAULT_URL = "http://localhost:18082";
    public static String RESERVATION_BASE_URL;

    public static final String PAYMENTS_MOCK_BASE_URL_PROPERTY_KEY = "PAYMENTS_MOCK_BASE_URL";
    public static final String PAYMENTS_MOCK_DEFAULT_URL = "http://localhost:18083";
    public static String PAYMENTS_MOCK_BASE_URL;

    public static String adminToken;
    public static Response lastResponse;
    public static Map<String, Object> lastParams;

    static {
        KIOSK_BASE_URL = System.getProperty(KIOSK_BASE_URL_PROPERTY_KEY, System.getenv().getOrDefault(KIOSK_BASE_URL_PROPERTY_KEY, KIOSK_DEFAULT_URL));
        ADMIN_BASE_URL = System.getProperty(ADMIN_BASE_URL_PROPERTY_KEY, System.getenv().getOrDefault(ADMIN_BASE_URL_PROPERTY_KEY, ADMIN_DEFAULT_URL));
        RESERVATION_BASE_URL = System.getProperty(RESERVATION_BASE_URL_PROPERTY_KEY, System.getenv().getOrDefault(RESERVATION_BASE_URL_PROPERTY_KEY, RESERVATION_DEFAULT_URL));
        PAYMENTS_MOCK_BASE_URL = System.getProperty(PAYMENTS_MOCK_BASE_URL_PROPERTY_KEY, System.getenv().getOrDefault(PAYMENTS_MOCK_BASE_URL_PROPERTY_KEY, PAYMENTS_MOCK_DEFAULT_URL));
    }


}
