package com.camping.tests.helper;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * 기본 API 요청 전송 유틸리티
 */
public class RequestSender {

    /**
     * 지정된 baseUrl로 GET 요청을 보냅니다.
     */
    public static Response get(String baseUrl, String endpoint) {
        return given()
                .baseUri(baseUrl)
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
    }
}
