package com.camping.tests.steps;

import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.그러면;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HealthCheckSteps {

    private static final Map<String, String> SERVICE_URLS = Map.of(
            "kiosk", System.getProperty("kiosk.base.url",
                    System.getenv().getOrDefault("KIOSK_BASE_URL", "http://localhost:18080")),
            "admin", System.getProperty("admin.base.url",
                    System.getenv().getOrDefault("ADMIN_BASE_URL", "http://localhost:18081")),
            "reservation", System.getProperty("reservation.base.url",
                    System.getenv().getOrDefault("RESERVATION_BASE_URL", "http://localhost:18082"))
    );

    private Response response;

    @만약("{string} 서비스의 {string}에 요청을 보낸다")
    public void 서비스에요청을보낸다(String service, String path) {
        String baseUrl = SERVICE_URLS.get(service);
        if (baseUrl == null) {
            throw new IllegalArgumentException("Unknown service: " + service);
        }
        response = RestAssured.get(baseUrl + path);
    }

    @그러면("성공 응답을 받는다")
    public void 성공응답을받는다() {
        assertEquals(200, response.statusCode());
    }
}