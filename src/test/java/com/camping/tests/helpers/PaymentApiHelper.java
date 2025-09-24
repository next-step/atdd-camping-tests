package com.camping.tests.helpers;

import static io.restassured.RestAssured.given;

import com.camping.tests.context.CommonContext;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

public class PaymentApiHelper {

    private static final String PAYMENTS_BASE_URL = System.getProperty("PAYMENTS_BASE_URL");

    public static Response createPayment(String amount, String cardNumber, String paymentMethod) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", amount);
        requestBody.put("cardNumber", cardNumber);
        requestBody.put("paymentMethod", paymentMethod);

        return given()
                .header("Authorization", "Bearer " + CommonContext.getAdminToken())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(PAYMENTS_BASE_URL + "/v1/payments");
    }

    public static Response confirmPayment(String paymentKey, String amount) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", paymentKey);
        requestBody.put("amount", amount);

        return given()
                .header("Authorization", "Bearer " + CommonContext.getAdminToken())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(PAYMENTS_BASE_URL + "/v1/payments/confirm");
    }
}
