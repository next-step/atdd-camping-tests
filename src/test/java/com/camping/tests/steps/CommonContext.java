package com.camping.tests.steps;

import java.util.Map;

public class CommonContext {

    private static final Map<String, String> SERVICE_URLS = Map.of(
            "kiosk", System.getProperty("kiosk.base.url",
                    System.getenv().getOrDefault("KIOSK_BASE_URL", "http://localhost:18080")),
            "admin", System.getProperty("admin.base.url",
                    System.getenv().getOrDefault("ADMIN_BASE_URL", "http://localhost:18081")),
            "reservation", System.getProperty("reservation.base.url",
                    System.getenv().getOrDefault("RESERVATION_BASE_URL", "http://localhost:18082"))
    );

    private String authToken;

    public String serviceUrl(String service) {
        return SERVICE_URLS.get(service);
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public String authToken() {
        return authToken;
    }

}
