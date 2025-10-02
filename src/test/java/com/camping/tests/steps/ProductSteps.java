package com.camping.tests.steps;

import com.camping.tests.helpers.ApiTestHelper;
import com.camping.tests.helpers.ContextHelper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ProductSteps {

    private static final String AUTH_TOKEN_KEY = "authToken";

    @When("{string} 서비스에 상품 목록 요청을 보낸다")
    public void 서비스에_상품_목록_요청을_보낸다(String service) {
        String envKey = service.toUpperCase() + "_BASE_URL";
        String baseUrl = ApiTestHelper.resolveBaseUrl(envKey, "http://localhost:18081");
        String targetUrl = ApiTestHelper.buildUrl(baseUrl, "/api/products");

        String authToken = ContextHelper.get(AUTH_TOKEN_KEY, String.class);

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(targetUrl);

        ContextHelper.set("response", response);
    }

    @Then("상품 목록 조회가 성공한다")
    public void 상품_목록_조회가_성공한다() {
        Response response = ContextHelper.get("response", Response.class);

        // status = 200
        int statusCode = response.getStatusCode();
        assert statusCode == 200 : "Expected 200, but got " + statusCode;

        // 배열 길이 >= 1
        int arraySize = response.jsonPath().getList("$").size();
        assert arraySize >= 1 : "Expected at least 1 product, but got " + arraySize;

        // 주요 필드 존재 확인
        String firstProductId = response.jsonPath().getString("[0].id");
        String firstProductName = response.jsonPath().getString("[0].name");
        String firstProductPrice = response.jsonPath().getString("[0].price");

        assert firstProductId != null : "Product id field is missing";
        assert firstProductName != null : "Product name field is missing";
        assert firstProductPrice != null : "Product price field is missing";
    }
}
