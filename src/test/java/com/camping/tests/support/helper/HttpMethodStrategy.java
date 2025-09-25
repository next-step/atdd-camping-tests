package com.camping.tests.support.helper;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public interface HttpMethodStrategy {
    <T> ExtractableResponse<Response> execute(RequestSpecification requestSpec, String url, T body);

    boolean supports(HttpMethod method);
}