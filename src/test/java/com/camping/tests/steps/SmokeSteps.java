package com.camping.tests.steps;

import io.cucumber.java.ko.그러면;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SmokeSteps {

    private static final Map<String, String> SERVICE_URLS = Map.of(
            "kiosk", System.getenv("KIOSK_BASE_URL") != null
                    ? System.getenv("KIOSK_BASE_URL") : "http://localhost:18081",
            "admin", System.getenv("ADMIN_BASE_URL") != null
                    ? System.getenv("ADMIN_BASE_URL") : "http://localhost:18082",
            "reservation", System.getenv("RESERVATION_BASE_URL") != null
                    ? System.getenv("RESERVATION_BASE_URL") : "http://localhost:18083"
    );

    @그러면("{string} 서비스의 {string} 엔드포인트가 성공 응답을 반환한다")
    public void 서비스의_엔드포인트가_성공_응답을_반환한다(String service, String endpoint) {
        String baseUrl = SERVICE_URLS.get(service);
        if (baseUrl == null) {
            throw new IllegalArgumentException("Unknown service: " + service);
        }

        Response response = RestAssured
                .given().baseUri(baseUrl)
                .when().get(endpoint);

        assertEquals(200, response.statusCode());
        assertEquals("OK", response.getBody().asString());
    }
}