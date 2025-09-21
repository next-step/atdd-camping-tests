package com.camping.common.support;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public final class CommonContext {

    public static final String KIOSK_BASE_URL_PROPERTY_KEY = "KIOSK_BASE_URL";
    public static final String KIOSK_DEFAULT_URL = "http://localhost:18080";
    public static String KIOSK_BASE_URL;

    public static final String ADMIN_BASE_URL_PROPERTY_KEY = "ADMIN_BASE_URL";
    public static final String ADMIN_DEFAULT_URL = "http://localhost:18081";
    public static String ADMIN_BASE_URL;

    public static String adminToken;
    public static RequestSpecification requestSpec;
    public static Response lastResponse;

    static {
        KIOSK_BASE_URL = System.getProperty(KIOSK_BASE_URL_PROPERTY_KEY, System.getenv().getOrDefault(KIOSK_BASE_URL_PROPERTY_KEY, KIOSK_DEFAULT_URL));
        ADMIN_BASE_URL = System.getProperty(ADMIN_BASE_URL_PROPERTY_KEY, System.getenv().getOrDefault(ADMIN_BASE_URL_PROPERTY_KEY, ADMIN_DEFAULT_URL));
    }


}
