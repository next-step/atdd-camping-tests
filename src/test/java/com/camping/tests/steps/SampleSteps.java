package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import org.awaitility.Awaitility;

import java.time.Duration;

import static io.restassured.RestAssured.given;

public class SampleSteps {

    public static final String KIOSK_BASE_URL = "KIOSK_BASE_URL";
    private String targetUrl;

    private static String resolveBaseUrl() {
        String env = System.getenv(KIOSK_BASE_URL);
        if (env != null && !env.isBlank()) {
            return env;
        }

        String prop = System.getProperty(KIOSK_BASE_URL);
        if (prop != null && !prop.isBlank()) {
            return prop;
        }
        return "http://localhost:18081";
    }

    @When("{string}에 요청을 보낸다")
    public void 요청을보낸다(String endpoint) {
        String baseUrl = resolveBaseUrl();
        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }
        targetUrl = baseUrl + endpoint;
        System.out.println("요청 URL = " + targetUrl);
    }

    @Then("성공 응답을 받는다")
    public void 성공응답을받는다() {
        // 최대 60초, 1초 간격으로 재시도
        Awaitility.await()
                .atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptions() // Connection refused, 404 등 무시하고 재시도
                .untilAsserted(() -> {
                    RestAssured.given()
                            .when()
                            .get(targetUrl)
                            .then()
                            .statusCode(200);
                });
    }
}


