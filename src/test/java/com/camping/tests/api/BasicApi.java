package com.camping.tests.api;

import com.camping.tests.context.HttpContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.util.Map;

public class BasicApi {
    protected final HttpContext httpContext;

    public BasicApi(HttpContext httpContext) {
        this.httpContext = httpContext;
    }

    public void get(String baseUrl, String path, Map<String, String> headers) {
        ExtractableResponse<Response> response = RestAssured.given()
                .log().all()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .headers(headers)
                .when()
                .get(path)
                .then()
                .log().all()
                .extract();

        httpContext.setResponse(response);
    }

    public void get(String baseUrl, String path) {
        ExtractableResponse<Response> response = RestAssured.given()
                .log().all()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .when()
                .get(path)
                .then()
                .log().all()
                .extract();

        httpContext.setResponse(response);
    }


    public void post(String baseUrl, String path, Object body, Map<String, String> headers) {
        ExtractableResponse<Response> response = RestAssured.given()
                .log().all()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .headers(headers)
                .body(body)
                .when()
                .post(path)
                .then()
                .log().all()
                .extract();

        httpContext.setResponse(response);
    }

    public void post(String baseUrl, String path, Object body) {
        ExtractableResponse<Response> response = RestAssured.given()
                .log().all()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(path)
                .then()
                .log().all()
                .extract();

        httpContext.setResponse(response);
    }
}
