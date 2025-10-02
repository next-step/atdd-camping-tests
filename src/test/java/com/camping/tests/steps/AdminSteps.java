package com.camping.tests.steps;

import com.camping.tests.helpers.ApiTestHelper;
import com.camping.tests.helpers.ContextHelper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import org.awaitility.Awaitility;

import java.time.Duration;

public class AdminSteps {

    private static final String TARGET_URL_KEY = "targetUrl";

    @When("{string} 서비스의 {string}에 요청을 보낸다")
    public void 서비스_요청을_보낸다(String service, String endpoint) {
        String envKey = service.toUpperCase() + "_BASE_URL";
        int defaultPort = getDefaultPort(service);
        String baseUrl = ApiTestHelper.resolveBaseUrl(envKey, "http://localhost:" + defaultPort);
        String targetUrl = ApiTestHelper.buildUrl(baseUrl, endpoint);
        ContextHelper.set(TARGET_URL_KEY, targetUrl);
    }

    @Then("{string} 서비스가 응답한다")
    public void 서비스가_응답한다(String service) {
        String targetUrl = ContextHelper.get(TARGET_URL_KEY, String.class);
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptions()
                .untilAsserted(() -> {
                    int statusCode = RestAssured.given()
                            .when()
                            .get(targetUrl)
                            .then()
                            .extract().statusCode();
                    assert statusCode == 401 || statusCode == 200 : "Expected 401 or 200, but got " + statusCode;
                });
    }

    private int getDefaultPort(String service) {
        switch (service.toLowerCase()) {
            case "kiosk":
                return 18081;
            case "admin":
                return 18082;
            case "reservation":
                return 18083;
            default:
                return 8080;
        }
    }
}
