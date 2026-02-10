package com.camping.tests.api;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class GenericApi {

    public ExtractableResponse<Response> 전체_URL로_GET_요청(String url) {
        return RestAssured
                .given()
                .when()
                    .get(url)
                .then()
                    .extract();
    }
}
