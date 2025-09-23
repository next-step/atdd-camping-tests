package com.camping.tests.helper;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class DeleteStrategy implements HttpMethodStrategy {

    @Override
    public <T> ExtractableResponse<Response> execute(RequestSpecification requestSpec, String url, T body) {
        return RestAssured.given()
                .spec(requestSpec)
                .when()
                .delete(url)
                .then()
                .log().all()
                .extract();
    }

    @Override
    public boolean supports(HttpMethod method) {
        return method == HttpMethod.DELETE;
    }
}