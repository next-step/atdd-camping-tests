package com.camping.tests.steps;

import com.camping.tests.config.TestConfig;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.그러면;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HealthCheckSteps {

    private Response response;

    @BeforeAll
    public static void waitForServices() {
        // 인증 없이 접근 가능한 엔드포인트로 헬스체크
        await().atMost(60, SECONDS)
               .pollInterval(2, SECONDS)
               .until(() -> isServiceReady(TestConfig.getAdminBaseUrl() + "/login"));

        await().atMost(60, SECONDS)
               .pollInterval(2, SECONDS)
               .until(() -> isServiceReady(TestConfig.getKioskBaseUrl()));

        await().atMost(60, SECONDS)
               .pollInterval(2, SECONDS)
               .until(() -> isServiceReady(TestConfig.getReservationBaseUrl()));
    }

    private static boolean isServiceReady(String url) {
        try {
            int statusCode = given().get(url).getStatusCode();
            return statusCode >= 200 && statusCode < 400;
        } catch (Exception e) {
            return false;
        }
    }

    @만약("{string} 서비스에 요청을 보낸다")
    public void 서비스에요청을보낸다(String serviceName) {
        String baseUrl = getBaseUrl(serviceName);
        response = given()
                .redirects().follow(false)  // redirect 따라가지 않음
                .when()
                .get(baseUrl);
    }

    @만약("{string} 서비스의 {string}에 요청을 보낸다")
    public void 서비스의경로에요청을보낸다(String serviceName, String path) {
        String baseUrl = getBaseUrl(serviceName);
        response = given()
                .when()
                .get(baseUrl + path);
    }

    @그러면("성공 응답을 받는다")
    public void 성공응답을받는다() {
        int statusCode = response.getStatusCode();
        assertTrue(statusCode >= 200 && statusCode < 400,
                "Expected 2xx or 3xx but got " + statusCode);
    }

    private String getBaseUrl(String serviceName) {
        return switch (serviceName.toLowerCase()) {
            case "admin" -> TestConfig.getAdminBaseUrl();
            case "kiosk" -> TestConfig.getKioskBaseUrl();
            case "reservation" -> TestConfig.getReservationBaseUrl();
            default -> throw new IllegalArgumentException("Unknown service: " + serviceName);
        };
    }
}