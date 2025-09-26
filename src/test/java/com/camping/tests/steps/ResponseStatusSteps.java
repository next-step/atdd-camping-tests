package com.camping.tests.steps;

import io.cucumber.java.en.Then;

import static com.camping.tests.helper.Assertions.assertSuccessResponse;
import static com.camping.tests.helper.Assertions.assertUnauthorziedResponse;
import static com.camping.tests.helper.Context.lastResponse;

public class ResponseStatusSteps {

    @Then("200 응답을 받는다")
    public void 응답을받는다_200() {
        assertSuccessResponse(lastResponse);
    }

    @Then("401 응답을 받는다")
    public void 응답을받는다_401() {
        assertUnauthorziedResponse(lastResponse);
    }
}
