package com.camping.tests.steps;

import com.camping.tests.config.TestConfig;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.조건;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KioskSteps {

    private Response response;
    private String adminToken;

    @조건("Admin에 다음 상품이 등록되어 있다")
    public void registerProductsToAdmin(DataTable dataTable) {
        // 1. Admin 로그인
        adminToken = loginToAdmin();

        // 2. 상품 등록
        List<Map<String, String>> products = dataTable.asMaps();
        for (Map<String, String> product : products) {
            registerProduct(product.get("name"), Integer.parseInt(product.get("price")));
        }
    }

    @만약("Kiosk에서 상품 목록을 조회한다")
    public void getProductsFromKiosk() {
        response = given()
                .when()
                .get(TestConfig.getKioskBaseUrl() + "/api/products");
    }

    @그러면("응답 상태 코드는 {int}이다")
    public void verifyStatusCode(int statusCode) {
        assertEquals(statusCode, response.getStatusCode());
    }

    @그리고("응답에 {int}개의 상품이 포함된다")
    public void verifyProductCount(int count) {
        response.then().body("$", hasSize(count));
    }

    @그리고("응답에 {string} 상품이 포함된다")
    public void verifyProductExists(String productName) {
        response.then().body("name", hasItem(productName));
    }

    private String loginToAdmin() {
        Response loginResponse = given()
                .contentType("application/json")
                .body("{\"username\": \"admin\", \"password\": \"admin123\"}")
                .when()
                .post(TestConfig.getAdminBaseUrl() + "/auth/login");

        return loginResponse.jsonPath().getString("accessToken");
    }

    private void registerProduct(String name, int price) {
        String body = String.format(
                "{\"name\": \"%s\", \"stockQuantity\": 10, \"price\": %d, \"productType\": \"SALE\"}",
                name, price);

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(body)
                .when()
                .post(TestConfig.getAdminBaseUrl() + "/admin/products");
    }
}