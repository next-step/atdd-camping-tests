package com.camping.tests.steps;

import com.camping.tests.helpers.ApiTestHelper;
import com.camping.tests.helpers.ContextHelper;
import io.cucumber.java.en.Then;

public class CommonSteps {

    private static final String TARGET_URL_KEY = "targetUrl";

    @Then("성공 응답을 받는다")
    public void 성공응답을받는다() {
        String targetUrl = ContextHelper.get(TARGET_URL_KEY, String.class);
        ApiTestHelper.assertGetSuccess(targetUrl);
    }
}
