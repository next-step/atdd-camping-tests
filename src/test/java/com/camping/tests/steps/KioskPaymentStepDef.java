package com.camping.tests.steps;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;

public class KioskPaymentStepDef {

    private int amount;
    private Response response;

    @Given("결제 금액이 {int}원인 장바구니가 있다")
    public void 결제금액이원인장바구니가있다(int amount) {
        this.amount = amount;
    }

    @When("결제키 {string} 와 주문번호 {string} 로 결제 승인을 요청한다")
    public void 결제키와주문번호로결제승인을요청한다(String paymentKey, String orderId) {
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId",    orderId,
                "amount",     amount,
                "items", List.of(Map.of(
                        "productId", 1,
                        "quantity", 1,
                        "lineTotal", amount
                ))
        );

        response = given()
                .contentType("application/json")
                .body(body)
                .when().post("http://localhost:8080/api/payments/confirm")
                .then().extract().response();
    }


    @Then("결제가 성공하며 결제 금액은 {int}원이다")
    public void 결제가성공하며결제금액은원이다(int expectedAmount) {
        assertThat(response.statusCode()).isEqualTo(200);
        boolean success = response.jsonPath().getBoolean("success");
        int paid = response.jsonPath().getInt("paidAmount");
        assertThat(success).isTrue();
        assertThat(paid).isEqualTo(expectedAmount);
    }

    @Then("결제가 실패한다.")
    public void 결제가실패한다() {
        int sc = response.statusCode();
        if (sc >= 200 && sc < 300) {
            boolean success = response.jsonPath().getBoolean("success");
            assertThat(success).isFalse();
        } else {
            assertThat(sc).isBetween(400, 599);
        }
    }
}
