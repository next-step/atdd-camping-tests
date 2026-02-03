package com.camping.tests.steps;

import com.camping.tests.clients.PaymentClient;
import com.camping.tests.context.ScenarioContext;
import io.cucumber.java.en.When;

public class PaymentSteps {
    private final ScenarioContext context;
    private final PaymentClient paymentClient;

    public PaymentSteps(ScenarioContext context) {
        this.context = context;
        this.paymentClient = new PaymentClient();
    }

    @When("결제 서비스의 {string}에 GET 요청을 보낸다")
    public void 결제_서비스의_GET_요청을_보낸다(String path) {
        var response = paymentClient.getFromPayment(path);
        context.setResponse(response);
    }
}
