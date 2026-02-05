package com.camping.tests.support;

import com.camping.tests.config.TestConfig;
import com.camping.tests.dto.LoginRequest;
import com.camping.tests.dto.ProductRequest;
import com.camping.tests.dto.StatusUpdateRequest;
import io.restassured.response.Response;

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

    public ProductResult createProduct(String name, int price) {
        login();
        String uniqueName = name + "_" + System.currentTimeMillis();
        var response = api.post(Admin.PRODUCTS, ProductRequest.sale(uniqueName, price));
        return new ProductResult(
                response.jsonPath().getInt("id"),
                response.jsonPath().getString("name")
        );
    }

    public Response getProducts() {
        login();
        return api.get(Admin.PRODUCTS);
    }

    public int getProductStock(int productId) {
        login();
        Response response = api.get(Admin.PRODUCTS);
        return response.jsonPath().getInt("find { it.id == " + productId + " }.stockQuantity");
    }

    public Response getSales() {
        login();
        return api.get(Admin.SALES);
    }

    public Response getReservations() {
        login();
        return api.get(Admin.RESERVATIONS);
    }

    public Response updateReservationStatus(long reservationId, String status) {
        login();
        String path = String.format(Admin.RESERVATION_STATUS, reservationId);
        return api.patch(path, new StatusUpdateRequest(status));
    }

    public Response lookupReservationByCode(String confirmationCode) {
        login();
        return api.get(Admin.RESERVATION_LOOKUP + "?code=" + confirmationCode);
    }

    public record ProductResult(int id, String name) {}
}