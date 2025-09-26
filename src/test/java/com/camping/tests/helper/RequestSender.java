package com.camping.tests.helper;

import io.restassured.response.Response;

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
        return given()
                .baseUri(baseUrl)
                .log().all() // 요청 정보 로깅
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(endpoint)
                .then()
                .log().all() // 모든 응답 로깅 (성공/실패 모두)
                .extract()
                .response();
    }
}
