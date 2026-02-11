package com.camping.tests.api;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class HealthApi {

    public ExtractableResponse<Response> 엔드포인트_조회(String baseUrl, String endpoint) {
        return RestAssured
                .given()
                    .baseUri(baseUrl)
                .when()
                    .get(endpoint)
                .then()
                    .extract();
    }
}
