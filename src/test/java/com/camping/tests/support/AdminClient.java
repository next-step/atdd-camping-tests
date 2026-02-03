package com.camping.tests.support;

import com.camping.tests.config.TestConfig;
import com.camping.tests.dto.LoginRequest;
import com.camping.tests.dto.ProductRequest;

import static com.camping.tests.support.Endpoints.Admin;

public class AdminClient {

    private final ApiClient api;
    private boolean loggedIn = false;

    public AdminClient() {
        this.api = new ApiClient(TestConfig.getAdminBaseUrl());
    }

    public AdminClient login() {
        if (!loggedIn) {
            String token = api.post(Admin.LOGIN, new LoginRequest(TestConfig.getAdminUsername(), TestConfig.getAdminPassword()))
                    .jsonPath().getString("accessToken");
            api.withToken(token);
            loggedIn = true;
        }
        return this;
    }

    public int createProduct(String name, int price) {
        login();
        return api.post(Admin.PRODUCTS, ProductRequest.sale(name, price))
                .jsonPath().getInt("id");
    }
}