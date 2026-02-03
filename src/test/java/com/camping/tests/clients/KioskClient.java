package com.camping.tests.clients;

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

    public static ExtractableResponse<Response> getProducts(String baseUrl) {
        return ApiClient.get(baseUrl + PRODUCTS_ENDPOINT);
    }

    public static ExtractableResponse<Response> createPayment(String baseUrl, PaymentCreateRequest request) throws JsonProcessingException {
        String requestBody = objectMapper.writeValueAsString(request);
        return ApiClient.post(baseUrl + PAYMENTS_ENDPOINT, requestBody);
    }

    public static ExtractableResponse<Response> confirmPayment(String baseUrl, PaymentConfirmRequest request) throws JsonProcessingException {
        String requestBody = objectMapper.writeValueAsString(request);
        return ApiClient.post(baseUrl + PAYMENTS_CONFIRM_ENDPOINT, requestBody);
    }
}
