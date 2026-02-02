package com.camping.tests.steps;

import io.cucumber.java.ko.만약;

import static io.restassured.RestAssured.given;

public class MultiServiceSmokeSteps {

    private static final String ADMIN_BASE_URL = getEnvOrDefault("ADMIN_BASE_URL", "http://localhost:18082");
    private static final String RESERVATION_BASE_URL = getEnvOrDefault("RESERVATION_BASE_URL", "http://localhost:18083");

    private final SharedContext context;

    public MultiServiceSmokeSteps(SharedContext context) {
        this.context = context;
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    @만약("Admin의 {string} 엔드포인트에 GET 요청을 보낸다")
    public void admin의_엔드포인트에_get_요청을_보낸다(String endpoint) {
        context.setResponse(given()
                .baseUri(ADMIN_BASE_URL)
                .when()
                .get(endpoint));
    }

    @만약("Reservation의 {string} 엔드포인트에 GET 요청을 보낸다")
    public void reservation의_엔드포인트에_get_요청을_보낸다(String endpoint) {
        context.setResponse(given()
                .baseUri(RESERVATION_BASE_URL)
                .when()
                .get(endpoint));
    }
}
