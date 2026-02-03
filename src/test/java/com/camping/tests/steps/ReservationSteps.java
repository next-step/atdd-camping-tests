package com.camping.tests.steps;

import com.camping.tests.clients.ApiClient;
import com.camping.tests.config.TestConfig;
import com.camping.tests.context.ScenarioContext;
import io.cucumber.java.en.When;

public class ReservationSteps {
    private final String reservationBaseUrl;
    private final ScenarioContext context;

    public ReservationSteps(ScenarioContext context) {
        this.context = context;
        this.reservationBaseUrl = TestConfig.getReservationBaseUrl();
    }

    @When("예약 서비스의 {string}에 GET 요청을 보낸다")
    public void 예약_서비스의_GET_요청을_보낸다(String path) {
        var response = ApiClient.get(reservationBaseUrl + path);
        context.setResponse(response);
    }
}
