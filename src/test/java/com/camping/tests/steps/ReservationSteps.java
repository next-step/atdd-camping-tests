package com.camping.tests.steps;

import com.camping.tests.clients.ReservationClient;
import com.camping.tests.context.ScenarioContext;
import io.cucumber.java.en.When;

public class ReservationSteps {
    private final ScenarioContext context;
    private final ReservationClient reservationClient;

    public ReservationSteps(ScenarioContext context) {
        this.context = context;
        this.reservationClient = new ReservationClient();
    }

    @When("예약 서비스의 {string}에 GET 요청을 보낸다")
    public void 예약_서비스의_GET_요청을_보낸다(String path) {
        var response = reservationClient.getFromReservation(path);
        context.setResponse(response);
    }
}
