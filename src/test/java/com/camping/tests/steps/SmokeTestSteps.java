package com.camping.tests.steps;

import io.cucumber.java.en.When;

import static com.camping.tests.helper.Context.*;
import static com.camping.tests.helper.RequestSender.get;

/**
 * 키오스크 애플리케이션 인수테스트를 위한 Step Definitions
 */
public class SmokeTestSteps {

    @When("키오스크 홈페이지 {string}에 요청을 보낸다")
    public void 키오스크홈페이지에요청을보낸다(String endpoint) {
        lastResponse = get(kioskBaseUrl, endpoint);
    }

    @When("관리자 홈페이지 {string}에 요청을 보낸다")
    public void 관리자홈페이지에요청을보낸다(String endpoint) {
        lastResponse = get(adminBaseUrl, endpoint);
    }

    @When("예약 홈페이지 {string}에 요청을 보낸다")
    public void 예약홈페이지에요청을보낸다(String endpoint) {
        lastResponse = get(reservationBaseUrl, endpoint);
    }
}

