package com.camping.tests.helper;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

public class StepContext {

    private static String accessToken;

    public static RequestSpecification getRequestSpecification() {
        return RestAssured.given()
                .baseUri("http://localhost:18081")
                .contentType("application/json")
                .log().all();
    }

    public static RequestSpecification getRequestSpecificationWithAccessToken() {
        return getRequestSpecification()
                .header("Authorization", "Bearer " + getAccessToken());
    }

    public static String getAccessToken() {
        if (accessToken == null) {
            // 기본 토큰 설정 (실제로는 로그인 API 호출해서 받아와야 함)
            accessToken = "dummy-token";
        }
        return accessToken;
    }

    public static void setAccessToken(String token) {
        accessToken = token;
    }
}