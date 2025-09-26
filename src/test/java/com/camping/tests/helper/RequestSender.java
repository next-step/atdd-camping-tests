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
                .baseUri(baseUrl)
                .log().all() // 요청 정보 로깅
                .when()
                .get(endpoint)
                .then()
                .log().all() // 모든 응답 로깅 (성공/실패 모두)
                .extract()
                .response();
    }

    public static Response post(String baseUrl, String endpoint, Map<String, Object> body) {
        return given()
                .baseUri(baseUrl)
                .contentType("application/json")
                .body(body)
                .log().all() // 요청 정보 로깅 (헤더, 본문 포함)
                .when()
                .post(endpoint)
                .then()
                .log().all() // 모든 응답 로깅 (성공/실패 모두)
                .extract()
                .response();
    }

    public static Response getWithAuth(String baseUrl, String endpoint, String authToken) {
        RequestSpecification request = given()
                .baseUri(baseUrl)
                .log().all(); // 요청 정보 로깅

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
                .log().all() // 모든 응답 로깅 (성공/실패 모두)
                .extract()
                .response();
    }

    public static Response getWithCookies(String baseUrl, String endpoint, Map<String, String> cookies) {
        RequestSpecification request = given()
                .baseUri(baseUrl)
                .log().all(); // 요청 정보 로깅

        if (cookies != null && !cookies.isEmpty()) {
            request = request.cookies(cookies);
        }

        return request
                .when()
                .get(endpoint)
                .then()
                .log().all() // 모든 응답 로깅 (성공/실패 모두)
                .extract()
                .response();
    }
}
