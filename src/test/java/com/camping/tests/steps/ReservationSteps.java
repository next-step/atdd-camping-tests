package com.camping.tests.steps;

import com.camping.tests.context.ScenarioContext;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Optional;

public class ReservationSteps {
    private final String reservationBaseUrl;
    private final ScenarioContext context;

    public ReservationSteps(ScenarioContext context) {
        this.context = context;
        this.reservationBaseUrl = Optional.ofNullable(System.getenv("RESERVATION_BASE_URL"))
                .orElse("http://localhost:8082");
    }

    @When("예약 서비스의 {string}에 GET 요청을 보낸다")
    public void 예약_서비스의_GET_요청을_보낸다(String path) {
        Response response = RestAssured.given()
                .when()
                .get(reservationBaseUrl + path);
        context.setResponse(response);
    }
}
