package com.camping.tests.steps;


import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.camping.tests.context.CommonContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

public class PaymentTestSteps {
    private Response response;
    private final String paymentBaseUrl;
    private final CommonContext context;

    public PaymentTestSteps() {
        this.context = new CommonContext();
        this.paymentBaseUrl = System.getProperty("PAYMENTS_BASE_URL");
    }

    @When("고객이 키오스크에서 유효한 카드로 결제를 요청한다")
    public void requestValidCardPayment() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", "10000");
        requestBody.put("cardNumber", "1234-5678-9012-3456");
        requestBody.put("paymentMethod", "CARD");

        response = given()
                .header("Authorization", "Bearer " + CommonContext.getAdminToken())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(paymentBaseUrl + "/v1/payments");

        context.setResponse(response);
    }

    @When("결제 서버가 일시적으로 사용할 수 없는 상태에서 결제를 요청한다")
    public void requestPaymentWhenServerError() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", "10000");
        requestBody.put("cardNumber", "1234-5678-9012-3456");
        requestBody.put("paymentMethod", "CARD");

        response = given()
                .log().all()
                .header("Authorization", "Bearer " + CommonContext.getAdminToken())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .log().all()
                .post(paymentBaseUrl + "/v1/payments/server-error");

        context.setResponse(response);
    }

    @Then("결제가 성공적으로 처리된다")
    public void verifyPaymentSuccess() {
        response.then().statusCode(200);
    }

    @Then("결제가 실패로 처리된다")
    public void verifyPaymentFailure() {
        response.then().statusCode(500);
    }

    @And("결제 성공 응답이 반환된다")
    public void verifyPaymentStatus() {
        boolean success = response.jsonPath().getBoolean("success");
        String message = response.jsonPath().getString("message");

        assertThat(success).isTrue();
        assertThat(message).isEqualTo("결제 생성 성공");
    }

    @And("결제 실패 응답에 오류 정보가 포함된다")
    public void verifyPaymentErrorResponse() {
        boolean success = response.jsonPath().getBoolean("success");
        String message = response.jsonPath().getString("message");

        assertThat(success).isFalse();
        assertThat(message).isEqualTo("결제 생성 실패");
    }

}
