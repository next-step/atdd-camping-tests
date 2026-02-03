package com.camping.tests.clients;

import com.camping.tests.config.TestConfig;
import com.camping.tests.dto.PaymentConfirmRequest;
import com.camping.tests.dto.PaymentCreateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

public class KioskClient {

    private static final String PRODUCTS_ENDPOINT = "/api/products";
    private static final String PAYMENTS_ENDPOINT = "/api/payments";
    private static final String PAYMENTS_CONFIRM_ENDPOINT = "/api/payments/confirm";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ApiClient api;

    public KioskClient() {
        this.api = new ApiClient(TestConfig.getKioskBaseUrl());
    }

    public ExtractableResponse<Response> get(String path) {
        return api.get(path);
    }

    public ExtractableResponse<Response> getProducts() {
        return api.get(PRODUCTS_ENDPOINT);
    }

    public ExtractableResponse<Response> createPayment(PaymentCreateRequest request) throws JsonProcessingException {
        return api.post(PAYMENTS_ENDPOINT, objectMapper.writeValueAsString(request));
    }

    public ExtractableResponse<Response> confirmPayment(PaymentConfirmRequest request) throws JsonProcessingException {
        return api.post(PAYMENTS_CONFIRM_ENDPOINT, objectMapper.writeValueAsString(request));
    }
}
