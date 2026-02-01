package com.camping.tests.steps;

import io.cucumber.java.ko.만약;
import io.restassured.RestAssured;

public class KioskSmokeSteps {

    private static final String KIOSK_BASE_URL = System.getenv("KIOSK_BASE_URL") != null
            ? System.getenv("KIOSK_BASE_URL")
            : "http://localhost:18081";

    private final SharedContext context;

    public KioskSmokeSteps(SharedContext context) {
        this.context = context;
    }

    @만약("Kiosk의 {string} 엔드포인트에 GET 요청을 보낸다")
    public void kiosk의_엔드포인트에_get_요청을_보낸다(String endpoint) {
        context.setResponse(RestAssured
                .given()
                .baseUri(KIOSK_BASE_URL)
                .when()
                .get(endpoint));
    }
}
