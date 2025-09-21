package com.camping.common;

import com.camping.common.support.CommonContext;
import io.cucumber.java.en.Then;

public class CommonSteps {

    @Then("성공 응답을 받는다")
    public void 성공응답을받는다() {
        CommonContext.lastResponse.then().statusCode(200);
    }
}
