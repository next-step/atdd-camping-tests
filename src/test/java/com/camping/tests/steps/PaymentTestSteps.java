package com.camping.tests.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.camping.tests.context.CommonContextHolder;
import com.camping.tests.helpers.KioskPaymentApiHelper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class PaymentTestSteps {
    private String paymentKey;
    private String orderId;

    private CommonContextHolder context() {
        return CommonContextHolder.getInstance();
    }

    @When("키오스크에 결제 생성을 요청한다")
    public void requestPaymentCreation() {
        Response response = KioskPaymentApiHelper.createPayment();
        context().setResponse(response);

        if (response.getStatusCode() == 200) {
            paymentKey = response.jsonPath().getString("paymentKey");
            orderId = response.jsonPath().getString("orderId");
        }
    }

    @Then("결제 생성이 성공한다")
    public void verifyPaymentCreationSuccess() {
        Response response = context().getResponse();
        response.then().statusCode(200);
    }

    @Then("결제 키와 주문 ID가 반환된다")
    public void verifyPaymentKeyAndOrderId() {
        assertThat(paymentKey).isNotNull();
        assertThat(orderId).isNotNull();
    }

    @When("키오스크에 결제 확정을 요청한다")
    public void requestPaymentConfirmation() {
        Response response = KioskPaymentApiHelper.confirmPayment(paymentKey, orderId, 10000);
        context().setResponse(response);
    }

    @When("키오스크에 금액 {string}원으로 결제 확정을 요청한다")
    public void requestPaymentConfirmationWithFailAmount(String amount) {
        Response response = KioskPaymentApiHelper.confirmPayment(paymentKey, orderId, Integer.parseInt(amount));
        context().setResponse(response);
    }

    @Then("결제가 성공이어야 한다")
    public void verifyPaymentSuccess() {
        Response response = context().getResponse();
        response.then().statusCode(200);
        boolean success = response.jsonPath().getBoolean("success");
        String transactionId = response.jsonPath().getString("transactionId");

        assertThat(success).isTrue();
        assertThat(transactionId).isEqualTo(paymentKey);
    }

    @Then("결제가 실패이어야 한다")
    public void verifyPaymentFailure() {
        Response response = context().getResponse();
        response.then().statusCode(200);
        boolean success = response.jsonPath().getBoolean("success");
        assertThat(success).isFalse();
    }
}
