package com.camping.tests.helper;

import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 키오스크 테스트 검증 유틸리티
 */
public class Assertions {
    
    public static void assertSuccessResponse(Response response) {
        assertEquals(200, response.getStatusCode());
    }
}
