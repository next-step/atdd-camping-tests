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
        // RestAssured의 기본 검증 기능을 사용하여 더 상세한 오류 메시지 제공
        try {
            assertEquals(SC_OK, response.getStatusCode(),
                    String.format("예상: 200 OK, 실제: %d %s\n응답 본문: %s",
                            response.getStatusCode(),
                            response.getStatusLine(),
                            response.getBody().asString()));
        } catch (AssertionError e) {
            // 검증 실패 시 응답 정보를 추가로 출력
            System.err.println("=== 응답 상태 검증 실패 ===");
            System.err.println("예상 상태: 200 OK");
            System.err.println("실제 상태: " + response.getStatusCode() + " " + response.getStatusLine());
            System.err.println("응답 본문: " + response.getBody().asString());
            System.err.println("========================");
            throw e;
        }
    }

    public static void assertUnauthorziedResponse(Response response) {
        try {
            assertEquals(SC_UNAUTHORIZED, response.getStatusCode(),
                    String.format("예상: 401 Unauthorized, 실제: %d %s\n응답 본문: %s",
                            response.getStatusCode(),
                            response.getStatusLine(),
                            response.getBody().asString()));
        } catch (AssertionError e) {
            // 검증 실패 시 응답 정보를 추가로 출력
            System.err.println("=== 응답 상태 검증 실패 ===");
            System.err.println("예상 상태: 401 Unauthorized");
            System.err.println("실제 상태: " + response.getStatusCode() + " " + response.getStatusLine());
            System.err.println("응답 본문: " + response.getBody().asString());
            System.err.println("========================");
            throw e;
        }
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
