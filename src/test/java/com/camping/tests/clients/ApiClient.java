package com.camping.tests.clients;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class ApiClient {

    private final RequestSpecification spec;

    public ApiClient(String baseUrl) {
        this.spec = RestAssured.given()
                .baseUri(baseUrl)
                .log().all();
    }

    public ExtractableResponse<Response> get(String path) {
        return spec
                .when()
                .get(path)
                .then()
                .log().all()
                .extract();
    }

    public ExtractableResponse<Response> post(String path, String requestBody) {
        return spec
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(path)
                .then()
                .log().all()
                .extract();
    }
}
