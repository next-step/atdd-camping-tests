package com.camping.tests.helper;

import io.restassured.response.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 테스트 검증 유틸리티
 */
public class Assertions {
    
    public static void assertSuccessResponse(Response response) {
        assertEquals(SC_OK, response.getStatusCode());
    }

    public static void assertUnauthorziedResponse(Response response) {
        assertEquals(SC_UNAUTHORIZED, response.getStatusCode());
    }

    public static void hasGivenFieldsAsNotNullInArray(Response response, String... fields) {
        List<Map<String, Object>> responseList = response.jsonPath().getList("$");
        Map<String, Object> firstResponse = responseList.get(0);

        Arrays.stream(fields)
                .forEach(field -> assertNotNull(firstResponse.get(field)));
    }

    public static void assertArrayLengthGreaterThanOrEqual(Response response, int expectedMinLength) {
        var array = response.jsonPath().getList("$");
        assertTrue(array.size() >= expectedMinLength,
                String.format("응답 배열의 길이(%d)는 %d 이상이어야 합니다", array.size(), expectedMinLength));
    }
}
