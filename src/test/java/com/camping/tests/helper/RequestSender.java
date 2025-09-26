package com.camping.tests.helper;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * 키오스크 API 요청 전송 유틸리티
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
}
