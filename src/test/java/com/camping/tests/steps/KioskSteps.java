package com.camping.tests.steps;

import com.camping.tests.context.ScenarioContext;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Optional;

public class KioskSteps {

    private final String kioskBaseUrl;
    private final ScenarioContext context;

    public KioskSteps(ScenarioContext context) {
        this.context = context;
        this.kioskBaseUrl = Optional.ofNullable(System.getenv("KIOSK_BASE_URL"))
                .orElse("http://localhost:18081");
    }

    @When("키오스크 서비스의 {string}에 GET 요청을 보낸다")
    public void 키오스크_서비스의_GET_요청을_보낸다(String path) {
        Response response = RestAssured.given()
                .when()
                .get(kioskBaseUrl + path);
        context.setResponse(response);
    }
}


