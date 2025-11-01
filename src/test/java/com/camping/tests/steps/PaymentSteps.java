package com.camping.tests.steps;

import com.camping.tests.helpers.ContextHelper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class PaymentSteps {

    private static final String KIOSK_BASE_URL = System.getenv("KIOSK_BASE_URL");
    private static final String PAYMENT_ID_KEY = "paymentId";
    private static final String PAYMENT_RESPONSE_KEY = "paymentResponse";

    @Given("키오스크에 결제 생성을 요청한다")
    public void 키오스크에_결제_생성을_요청한다() {
        String items = "[{\"productId\":1,\"productName\":\"랜턴\",\"unitPrice\":30000,\"quantity\":1}]";
        String requestBody = "{"
                + "\"items\":" + items + ","
                + "\"paymentMethod\":\"CARD\""
                + "}";

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(KIOSK_BASE_URL + "/api/payments")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String paymentKey = response.jsonPath().getString("paymentKey");
        String orderId = response.jsonPath().getString("orderId");
        ContextHelper.set("paymentKey", paymentKey);
        ContextHelper.set("orderId", orderId);
        ContextHelper.set("items", items);
    }

    @And("키오스크에 결제 확정을 요청한다")
    public void 키오스크에_결제_확정을_요청한다() {
        String paymentKey = ContextHelper.get("paymentKey", String.class);
        String orderId = ContextHelper.get("orderId", String.class);
        String items = ContextHelper.get("items", String.class);

        String requestBody = "{"
                + "\"paymentKey\":\"" + paymentKey + "\","
                + "\"orderId\":\"" + orderId + "\","
                + "\"items\":" + items
                + "}";

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(KIOSK_BASE_URL + "/api/payments/confirm")
                .then()
                .extract()
                .response();

        ContextHelper.set(PAYMENT_RESPONSE_KEY, response);
    }

    @Given("키오스크에 큰 금액으로 결제 생성을 요청한다")
    public void 키오스크에_큰_금액으로_결제_생성을_요청한다() {
        String items = "[{\"productId\":1,\"productName\":\"랜턴\",\"unitPrice\":999999,\"quantity\":1}]";
        String requestBody = "{"
                + "\"items\":" + items + ","
                + "\"paymentMethod\":\"CARD\""
                + "}";

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(KIOSK_BASE_URL + "/api/payments")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String paymentKey = response.jsonPath().getString("paymentKey");
        String orderId = response.jsonPath().getString("orderId");
        ContextHelper.set("paymentKey", paymentKey);
        ContextHelper.set("orderId", orderId);
        ContextHelper.set("items", items);
    }

    @Then("결제가 성공이어야 한다")
    public void 결제가_성공이어야_한다() {
        Response response = ContextHelper.get(PAYMENT_RESPONSE_KEY, Response.class);

        int statusCode = response.statusCode();
        assert statusCode == 200 : "Expected 200, but got " + statusCode;

        Boolean success = response.jsonPath().getBoolean("success");
        assert success != null && success : "Expected success to be true, but got " + success;

        String transactionId = response.jsonPath().getString("transactionId");
        assert transactionId != null && !transactionId.isEmpty() : "Expected transactionId to be present";
    }

    @Then("결제가 실패이어야 한다")
    public void 결제가_실패이어야_한다() {
        Response response = ContextHelper.get(PAYMENT_RESPONSE_KEY, Response.class);

        int statusCode = response.statusCode();
        assert statusCode == 200 : "Expected 200, but got " + statusCode;

        Boolean success = response.jsonPath().getBoolean("success");
        assert success != null && !success : "Expected success to be false, but got " + success;

        String message = response.jsonPath().getString("message");
        assert message != null && !message.isEmpty() : "Expected error message to be present";
    }
}
