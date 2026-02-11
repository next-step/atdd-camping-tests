package com.camping.tests.api;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentApi {

    public ExtractableResponse<Response> 결제_생성(String baseUrl, Map<String, Object> cartItemFixture, String paymentMethod) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("items", List.of(cartItemFixture));
        requestBody.put("paymentMethod", paymentMethod);

        return RestAssured
                .given()
                    .baseUri(baseUrl)
                    .contentType("application/json")
                    .body(requestBody)
                .when()
                    .post("/api/payments")
                .then()
                    .extract();
    }

    public ExtractableResponse<Response> 결제_확정(
            String baseUrl,
            String paymentKey,
            String orderId,
            int amount,
            Map<String, Object> cartItemFixture
    ) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", paymentKey);
        requestBody.put("orderId", orderId);
        requestBody.put("amount", amount);
        requestBody.put("items", List.of(cartItemFixture));

        return RestAssured
                .given()
                    .baseUri(baseUrl)
                    .contentType("application/json")
                    .body(requestBody)
                .when()
                    .post("/api/payments/confirm")
                .then()
                    .extract();
    }
}
