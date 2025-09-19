package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("NonAsciiCharacters")
public class KioskHealthSteps {

    private static final int MAX_RETRY_COUNT = 10;
    private static final int RETRY_INTERVAL_MS = 100;

    private Response response;

    @When("키오스크 서비스 루트 경로에 요청을 보내면")
    public void 키오스크_서비스_루트_경로에_요청을_보내면() {
        response = sendRequestWithRetry();
    }

    @Then("200 응답을 받는다")
    public void _200_응답을_받는다() {
        assertEquals(200, response.getStatusCode());
    }

    private Response sendRequestWithRetry() {
        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                Response response = given().get("http://localhost:18081/");
                if (response.getStatusCode() == 200) { return response; }
            } catch (Exception ignored) { }

            if (attempt < MAX_RETRY_COUNT) {
                try {
                    Thread.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }
        throw new RuntimeException("키오스크 서비스 응답 실패");
    }
}
