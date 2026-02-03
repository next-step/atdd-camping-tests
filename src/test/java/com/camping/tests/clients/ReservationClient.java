package com.camping.tests.clients;

import com.camping.tests.config.TestConfig;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

public class ReservationClient {
    private final ApiClient api;

    public ReservationClient() {
        this.api = new ApiClient(TestConfig.getReservationBaseUrl());
    }

    public ExtractableResponse<Response> getFromReservation(String path) {
        return api.get(path);
    }
}
