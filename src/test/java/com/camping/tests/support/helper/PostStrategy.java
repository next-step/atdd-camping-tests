package com.camping.tests.support.helper;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class PostStrategy implements HttpMethodStrategy {

    @Override
    public <T> ExtractableResponse<Response> execute(RequestSpecification requestSpec, String url, T body) {
        RequestSpecification given = RestAssured.given().spec(requestSpec);
        if (body != null) {
            given = given.body(body);
        }

        return given.when()
                .post(url)
                .then()
                .log().all()
                .extract();
    }

    @Override
    public boolean supports(HttpMethod method) {
        return method == HttpMethod.POST;
    }
}