package com.camping.tests.support.fixture;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.camping.tests.support.client.ApiClientFactory;
import static org.assertj.core.api.Assertions.assertThat;

public class KioskTestFixture {
    public static ExtractableResponse<Response> 키오스크_상품_목록_조회() {
        ExtractableResponse<Response> response = ApiClientFactory.kiosk()
                .get("/api/products")
                .needAuth()
                .execute();
        assertThat(response.statusCode()).isEqualTo(200);
        return response;
    }

    public static void 상품_목록_개수_검증(ExtractableResponse<Response> response, int expectedMinCount) {
        List<Map<String, Object>> products = response.jsonPath().getList("$");
        assertThat(products.size()).isGreaterThanOrEqualTo(expectedMinCount);
    }

    public static void 상품_기본_필드_검증_legacy(ExtractableResponse<Response> response) {
        // e2e.feature에서 요구하는 필드명과 실제 API 응답 필드명 매핑해서 검증
        List<Map<String, Object>> products = response.jsonPath().getList("$");
        assertThat(products).isNotEmpty();

        for (Map<String, Object> product : products) {
            // 실제 API 응답의 필드명으로 검증 (name, price, stockQuantity, productType)
            // 하지만 e2e.feature에서는 "이름, 가격, 수량, 타입"이라고 표현됨
            assertThat(product.get("name")).as("상품 이름").isNotNull();
            assertThat(product.get("price")).as("상품 가격").isNotNull();
            assertThat(product.get("stockQuantity")).as("상품 수량").isNotNull();
            assertThat(product.get("productType")).as("상품 타입").isNotNull();
        }
    }

    // 상품 구매 시도
    public static ExtractableResponse<Response> Kiosk_상품_구매_시도(Map<String, String> purchaseData) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("productId", Long.parseLong(purchaseData.get("productId")));
        requestBody.put("quantity", Integer.parseInt(purchaseData.get("quantity")));

        return ApiClientFactory.kiosk()
            .post("/api/purchases")
            .body(requestBody)
            .needAuth()
            .execute();
    }

    // 키오스크에서 상품 정보 일치 검증
    public static void Kiosk_상품_정보_일치_검증(ExtractableResponse<Response> productListResponse, Long createdProductId, Map<String, String> expectedProductData) {
        List<Map<String, Object>> products = productListResponse.jsonPath().getList("$");

        Map<String, Object> foundProduct = products.stream()
            .filter(product -> ((Integer) product.get("id")).longValue() == createdProductId)
            .findFirst()
            .orElseThrow(() -> new AssertionError("생성된 상품을 Kiosk에서 찾을 수 없습니다."));

        assertThat(foundProduct.get("name")).isEqualTo(expectedProductData.get("name"));
        assertThat(foundProduct.get("price")).isEqualTo(Long.parseLong(expectedProductData.get("expectedPrice")));
        assertThat(foundProduct.get("stockQuantity")).isEqualTo(Integer.parseInt(expectedProductData.get("expectedStock")));
    }
}