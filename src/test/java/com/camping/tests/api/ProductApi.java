package com.camping.tests.api;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class ProductApi {

    public ExtractableResponse<Response> 상품_목록_조회(String baseUrl) {
        return RestAssured
                .given()
                    .baseUri(baseUrl)
                .when()
                    .get("/api/products")
                .then()
                    .extract();
    }
}
