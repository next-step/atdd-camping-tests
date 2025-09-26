package com.camping.tests.helper;

import io.restassured.response.Response;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 키오스크 테스트 검증 유틸리티
 */
public class Assertions {
    
    public static void assertSuccessResponse(Response response) {
        assertEquals(SC_OK, response.getStatusCode());
    }

    public static void assertUnauthorziedResponse(Response response) {
        assertEquals(SC_UNAUTHORIZED, response.getStatusCode());
    }
}
