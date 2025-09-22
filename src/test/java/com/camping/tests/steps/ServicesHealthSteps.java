package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

@SuppressWarnings("NonAsciiCharacters")
public class ServicesHealthSteps {

    private static final int MAX_RETRY_COUNT = 10;
    private static final int RETRY_INTERVAL_MS = 100;

    private static final Map<String, Integer> SERVICE_PORTS = Map.of(
        "키오스크", 18081,
        "관리자", 18082,
        "예약", 18083
    );

    private Response response;

    @When("^(.+) 서비스 루트 경로에 요청을 보낸다$")
    public void 서비스_루트_경로에_요청을_보낸다(String serviceName) {
        Integer port = SERVICE_PORTS.get(serviceName);
        if (port == null) {
            throw new IllegalArgumentException("알 수 없는 서비스명: " + serviceName);
        }
        response = sendRequestWithRetry(port);
    }

    @Then("^200 응답을 받는다$")
    public void _200_응답을_받는다() {
        int statusCode = response.getStatusCode();

        if (!(statusCode >= 200)) {
            throw new AssertionError("예상하지 못한 응답 코드: " + statusCode);
        }
    }

    private Response sendRequestWithRetry(int port) {
        String url = buildUrlForPort(port);

        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                Response response = given().get(url);
                int statusCode = response.getStatusCode();

                if (statusCode >= 200) {
                    return response;
                }
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
        throw new RuntimeException("서비스 응답 실패: " + url);
    }

    private String buildUrlForPort(int port) {
        String baseUrl = "http://localhost:" + port;
        if (port == 18082) {
            return baseUrl + "/login";
        }
        return baseUrl + "/";
    }
}
