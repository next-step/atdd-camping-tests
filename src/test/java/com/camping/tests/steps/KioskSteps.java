package com.camping.tests.steps;

import com.camping.tests.helper.*;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static com.camping.tests.helper.KioskAssertions.*;
import static com.camping.tests.helper.KioskContext.lastResponse;
import static com.camping.tests.helper.KioskRequestSender.*;

/**
 * 키오스크 애플리케이션 인수테스트를 위한 Step Definitions
 */
public class KioskSteps {

    @Given("키오스크 애플리케이션이 준비되어 있다")
    public void 키오스크_애플리케이션이_준비되어_있다() {
        KioskApplicationWaiter.waitForApplicationReady();
    }

    @When("키오스크 홈페이지 {string}에 요청을 보낸다")
    public void 키오스크_홈페이지에_요청을_보낸다(String endpoint) {
        lastResponse = get(endpoint);
    }

    @Then("200 응답을 받는다")
    public void 응답을_받는다() {
        assertSuccessResponse(lastResponse);
    }
}

