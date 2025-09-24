package com.camping.tests.helpers;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KioskPaymentApiHelper {

    private static final String KIOSK_BASE_URL = System.getProperty("KIOSK_BASE_URL");

    public static Response createPayment() {
        Map<String, Object> item = new HashMap<>();
        item.put("productId", 1);
        item.put("productName", "테스트 상품");
        item.put("unitPrice", 10000);
        item.put("quantity", 1);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("items", List.of(item));
        requestBody.put("paymentMethod", "CARD");

        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(KIOSK_BASE_URL + "/api/payments");
    }

    public static Response confirmPayment(String paymentKey, String orderId, int amount) {
        Map<String, Object> item = new HashMap<>();
        item.put("productId", 1);
        item.put("productName", "테스트 상품");
        item.put("unitPrice", amount);
        item.put("quantity", 1);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", paymentKey);
        requestBody.put("orderId", orderId);
        requestBody.put("amount", amount);
        requestBody.put("items", List.of(item));

        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(KIOSK_BASE_URL + "/api/payments/confirm");
    }
}
