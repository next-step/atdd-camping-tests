package com.camping.tests.support;

import com.camping.tests.config.TestConfig;
import com.camping.tests.dto.CartItem;
import com.camping.tests.dto.PaymentConfirmRequest;
import com.camping.tests.dto.PaymentCreateRequest;
import io.restassured.response.Response;

import java.util.List;

import static com.camping.tests.support.Endpoints.Kiosk;

public class KioskClient {

    private final ApiClient api;

    public KioskClient() {
        this.api = new ApiClient(TestConfig.getKioskBaseUrl());
    }

    public Response getProducts() {
        return api.get(Kiosk.PRODUCTS);
    }

    public PaymentResult createPayment(List<CartItem> items) {
        Response response = api.post(Kiosk.PAYMENTS, PaymentCreateRequest.card(items));
        return new PaymentResult(
                response.jsonPath().getString("paymentKey"),
                response.jsonPath().getString("orderId"),
                response.jsonPath().getInt("amount")
        );
    }

    public Response confirmPayment(String paymentKey, String orderId, int amount, List<CartItem> items) {
        return api.post(Kiosk.PAYMENTS_CONFIRM,
                new PaymentConfirmRequest(paymentKey, orderId, amount, items));
    }

    public record PaymentResult(String paymentKey, String orderId, int amount) {}
}