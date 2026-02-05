package com.camping.tests.steps;

import io.cucumber.java.ko.만약;

import java.util.IllformedLocaleException;
import java.util.Map;
import static com.camping.tests.config.TestConfig.*;
import static io.restassured.RestAssured.given;

public class ServiceSteps {

    private static final Map<String, String> SERVICE_URLS = Map.of(
            "Kiosk", KIOSK_BASE_URL,
            "Admin", ADMIN_BASE_URL,
            "Reservation", RESERVATION_BASE_URL
    );

    private final SharedContext context;

    public ServiceSteps(SharedContext context) {
        this.context = context;
    }

    @만약("{word}의 {string} 엔드포인트에 GET 요청을 보낸다")
    public void 서비스의_엔드포인트에_GET_요청을_보낸다(String serviceName, String endpoint) {
        String baseUrl = SERVICE_URLS.get(serviceName);
        if (baseUrl == null) {
            throw new IllformedLocaleException("Unknown service name: " + serviceName);
        }

        context.setResponse(
                given()
                        .baseUri(baseUrl)
                        .when()
                        .get(endpoint)
        );
    }
}
