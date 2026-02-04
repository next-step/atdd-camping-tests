package com.camping.tests.clients;

import com.camping.tests.config.TestConfig;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

public class PaymentClient {
    private final ApiClient api;

    public PaymentClient() {
        this.api = new ApiClient(TestConfig.getPaymentBaseUrl());
    }

    public ExtractableResponse<Response> getFromPayment(String path) {
        return api.get(path);
    }
}
