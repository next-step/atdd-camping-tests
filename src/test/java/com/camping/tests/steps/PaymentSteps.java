package com.camping.tests.steps;

import com.camping.tests.clients.ApiClient;
import com.camping.tests.context.ScenarioContext;
import io.cucumber.java.en.When;
import java.util.Optional;

public class PaymentSteps {
    private final String paymentBaseUrl;
    private final ScenarioContext context;

    public PaymentSteps(ScenarioContext context) {
        this.context = context;
        this.paymentBaseUrl = Optional.ofNullable(System.getenv("PAYMENT_BASE_URL"))
                .orElse("http://localhost:8084");
    }

    @When("결제 서비스의 {string}에 GET 요청을 보낸다")
    public void 결제_서비스의_GET_요청을_보낸다(String path) {
        var response = ApiClient.get(paymentBaseUrl + path);
        context.setResponse(response);
    }
}
