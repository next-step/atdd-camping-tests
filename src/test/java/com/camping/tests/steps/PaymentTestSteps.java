package com.camping.tests.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.camping.tests.context.CommonContext;
import com.camping.tests.helpers.PaymentApiHelper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class PaymentTestSteps {
    private Response response;
    private String paymentKey;
    private final CommonContext context;

    public PaymentTestSteps() {
        this.context = new CommonContext();
    }

    @When("키오스크에 결제 생성을 요청한다")
    public void requestPaymentCreation() {
        response = PaymentApiHelper.createPayment("10000", "1234-5678-9012-3456", "CARD");

        if (response.getStatusCode() == 200) {
            paymentKey = response.jsonPath().getString("paymentKey");
        }
        context.setResponse(response);
    }

    @When("키오스크에 결제 확정을 요청한다")
    public void requestPaymentConfirmation() {
        response = PaymentApiHelper.confirmPayment(paymentKey, "10000");
        context.setResponse(response);
    }

    @When("키오스크에 금액 {string}원으로 결제 확정을 요청한다")
    public void requestPaymentConfirmationWithFailAmount(String amount) {
        response = PaymentApiHelper.confirmPayment(paymentKey, amount);
        context.setResponse(response);
    }

    @Then("결제가 성공이어야 한다")
    public void verifyPaymentSuccess() {
        response.then().statusCode(200);
        boolean success = response.jsonPath().getBoolean("success");
        String transactionId = response.jsonPath().getString("transactionId");

        assertThat(success).isTrue();
        assertThat(transactionId).isEqualTo(paymentKey);
    }

    @Then("결제가 실패이어야 한다")
    public void verifyPaymentFailure() {
        response.then().statusCode(400);
        boolean success = response.jsonPath().getBoolean("success");
        assertThat(success).isFalse();
    }

}
