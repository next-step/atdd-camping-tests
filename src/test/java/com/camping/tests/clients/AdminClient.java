package com.camping.tests.clients;

import com.camping.tests.config.TestConfig;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

public class AdminClient {
    private final ApiClient api;

    public AdminClient() {
        this.api = new ApiClient(TestConfig.getAdminBaseUrl());
    }

    public ExtractableResponse<Response> getFromAdmin(String path) {
        return api.get(path);
    }
}
