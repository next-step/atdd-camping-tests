package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static com.camping.tests.helper.Assertions.assertSuccessResponse;
import static com.camping.tests.helper.Context.lastResponse;
import static com.camping.tests.helper.ReservationRequestSender.get;

/**
 * 예약 시스템 애플리케이션 인수테스트를 위한 Step Definitions
 */
public class ReservationSteps {

    @When("예약 시스템 홈페이지 {string}에 요청을 보낸다")
    public void 예약_시스템_홈페이지에_요청을_보낸다(String endpoint) {
        lastResponse = get(endpoint);
    }

    @Then("200 응답을 받는다")
    public void 응답을_받는다() {
        assertSuccessResponse(lastResponse);
    }
}
