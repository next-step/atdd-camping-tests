package com.camping.tests.helper;

import io.restassured.response.Response;

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

    public static void assertHasAuthToken(Response response) {
        // JWT 토큰이 응답 본문에 있는지 확인
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains("token") || responseBody.contains("accessToken") ||
                        !response.getCookies().isEmpty(),
                "응답에 인증 토큰 또는 쿠키가 포함되어야 합니다");
    }

    public static void assertProductListResponse(Response response) {
        assertEquals(SC_OK, response.getStatusCode(), "상품 목록 조회 응답은 200이어야 합니다");

        List<Map<String, Object>> products = response.jsonPath().getList("$");
        assertNotNull(products, "상품 목록이 null이면 안됩니다");
        assertTrue(products.size() >= 1, "상품 목록의 길이는 1 이상이어야 합니다");

        // 첫 번째 상품의 주요 필드 확인
        Map<String, Object> firstProduct = products.get(0);
        assertProductFields(firstProduct);
    }

    private static void assertProductFields(Map<String, Object> product) {
        assertTrue(product.containsKey("id"), "상품에 id 필드가 있어야 합니다");
        assertTrue(product.containsKey("name"), "상품에 name 필드가 있어야 합니다");
        assertTrue(product.containsKey("price"), "상품에 price 필드가 있어야 합니다");

        assertNotNull(product.get("id"), "상품 id는 null이면 안됩니다");
        assertNotNull(product.get("name"), "상품 name은 null이면 안됩니다");
        assertNotNull(product.get("price"), "상품 price는 null이면 안됩니다");
    }

    public static void assertArrayLengthGreaterThanOrEqual(Response response, int expectedMinLength) {
        List<Object> array = response.jsonPath().getList("$");
        assertNotNull(array, "응답 배열이 null이면 안됩니다");
        assertTrue(array.size() >= expectedMinLength,
                String.format("응답 배열의 길이(%d)는 %d 이상이어야 합니다", array.size(), expectedMinLength));
    }
}
