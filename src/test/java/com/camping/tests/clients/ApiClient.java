package com.camping.tests.clients;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

public class ApiClient {

    public static ExtractableResponse<Response> get(String url) {
        return RestAssured.given()
                .log().all()
                .when()
                    .get(url)
                .then()
                    .log().all()
                    .extract();
    }
}
