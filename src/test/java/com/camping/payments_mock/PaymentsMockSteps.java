package com.camping.payments_mock;

import com.camping.common.support.ApiHelper;
import com.camping.common.support.CommonContext;
import io.cucumber.core.options.CurlOption.HttpMethod;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;

public class PaymentsMockSteps {

    public PaymentsMockSteps() {
        RestAssured.baseURI = CommonContext.PAYMENTS_MOCK_BASE_URL;
    }

    @When("결제 Mocking 시스템에 헬스체크를 요청한다")
    public void 결제Mocking시스템에헬스체크를요청한다() {
        CommonContext.lastResponse = ApiHelper.request(HttpMethod.POST, "/payments-mock-smoke-test", null);
    }
}
