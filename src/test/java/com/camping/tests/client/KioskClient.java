package com.camping.tests.client;

import com.camping.tests.CommonContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KioskClient {
    private static final String KIOSK_BASE_URL = "http://localhost:18081";

    public Response getProducts() {
        Response response = RestAssured.given()
            .contentType(ContentType.JSON)
            .when().get(KIOSK_BASE_URL + "/api/products")
            .then().log().all()
            .extract().response();

        return response;
    }

    public Response createPayment() {
        Map<String, Object> item = new HashMap<>();
        item.put("productId", 1L);
        item.put("productName", "ProductA");
        item.put("unitPrice", 1000);
        item.put("quantity", 1);

        Map<String, Object> body = new HashMap<>();
        body.put("items", List.of(item));
        body.put("paymentMethod", "CARD"); // or "CASH"

        Response response = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(body)
            .when().post(KIOSK_BASE_URL + "/api/payments")
            .then().log().all()
            .extract().response();
        CommonContext.orderId = response.jsonPath().getString("orderId");
        CommonContext.paymentKey = response.jsonPath().getString("paymentKey");
        CommonContext.isSuccess = response.jsonPath().getString("success");
        CommonContext.responseMessage = response.jsonPath().getString("message");
        return response;
    }

    public Response confirmPayment() {
        String paymentKey = CommonContext.paymentKey;
        String orderId = CommonContext.orderId;

        Map<String, Object> item = new HashMap<>();
        item.put("productId", 1L);
        item.put("productName", "ProductA");
        item.put("unitPrice", 1000);
        item.put("quantity", 1);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", orderId);
        body.put("amount", 1000);
        body.put("items", List.of(item));

        Response response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(body)
            .when().post(KIOSK_BASE_URL + "/api/payments/confirm")
            .then().log().all()
            .extract().response();
        CommonContext.isSuccess = response.jsonPath().getString("success");
        CommonContext.responseMessage = response.jsonPath().getString("message");
        return response;
    }

    public Response confirmPaymentWithAmount(int amount) {
        String paymentKey = CommonContext.paymentKey;
        String orderId = CommonContext.orderId;

        Map<String, Object> item = new HashMap<>();
        item.put("productId", 1L);
        item.put("productName", "ProductA");
        item.put("unitPrice", 1000);
        item.put("quantity", 1);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", orderId);
        body.put("amount", amount);
        body.put("items", List.of(item));

        Response response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(body)
            .when().post(KIOSK_BASE_URL + "/api/payments/confirm")
            .then().log().all()
            .extract().response();
        CommonContext.isSuccess = response.jsonPath().getString("success");
        CommonContext.responseMessage = response.jsonPath().getString("message");
        return response;
    }
}
