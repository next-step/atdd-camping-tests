package com.camping.tests.helper;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * API 요청 전송 유틸리티
 */
public class RequestSender {

    public static Response get(String baseUrl, String endpoint) {
        return given()
                .when()
                .baseUri(baseUrl)
                .get(endpoint)
                .then()
                .extract()
                .response();
    }

    public static Response post(String baseUrl, String endpoint, Map<String, Object> body) {
        return given()
                .contentType("application/json")
                .body(body)
                .when()
                .baseUri(baseUrl)
                .post(endpoint)
                .then()
                .extract()
                .response();
    }

    public static Response getWithAuth(String baseUrl, String endpoint, String authToken) {
        RequestSpecification request = given().baseUri(baseUrl);

        if (authToken != null && !authToken.isEmpty()) {
            // JWT 토큰인 경우 Authorization 헤더에 Bearer 토큰으로 추가
            if (authToken.startsWith("eyJ")) {
                request = request.header("Authorization", "Bearer " + authToken);
            } else {
                // 쿠키인 경우 Cookie 헤더에 추가
                request = request.cookie("JSESSIONID", authToken);
            }
        }

        return request
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
    }

    public static Response getWithCookies(String baseUrl, String endpoint, Map<String, String> cookies) {
        RequestSpecification request = given().baseUri(baseUrl);

        if (cookies != null && !cookies.isEmpty()) {
            request = request.cookies(cookies);
        }

        return request
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
    }
}
