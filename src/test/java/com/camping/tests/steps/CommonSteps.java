package com.camping.tests.steps;

import com.camping.tests.helpers.ApiTestHelper;
import com.camping.tests.helpers.ContextHelper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import org.awaitility.Awaitility;

import java.time.Duration;

public class CommonSteps {

    private static final String TARGET_URL_KEY = "targetUrl";

    @Then("성공 응답을 받는다")
    public void 성공응답을받는다() {
        String targetUrl = ContextHelper.get(TARGET_URL_KEY, String.class);
        ApiTestHelper.assertGetSuccess(targetUrl);
    }

    @When("{string} 서비스의 {string}에 요청을 보낸다")
    public void 서비스_요청을_보낸다(String service, String endpoint) {
        String envKey = service.toUpperCase() + "_BASE_URL";
        String baseUrl = System.getenv(envKey);
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(envKey + " environment variable is not set");
        }
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
}
