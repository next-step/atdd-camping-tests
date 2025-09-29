package com.camping.tests.support.fixture;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import com.camping.tests.support.client.ApiClientFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminTestFixture {

    // 상품 생성 API 호출
    public static ExtractableResponse<Response> Admin_상품_생성(Map<String, String> productData) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", productData.get("name"));
        requestBody.put("price", Long.parseLong(productData.get("price")));
        requestBody.put("stockQuantity", Integer.parseInt(productData.get("stockQuantity")));
        requestBody.put("productType", productData.get("productType"));

        ExtractableResponse<Response> response = ApiClientFactory.admin()
            .post("/admin/products")
            .body(requestBody)
            .needAuth()
            .execute();

        assertThat(response.statusCode()).isEqualTo(201);
        return response;
    }

    // 생성된 상품 ID 추출
    public static Long Admin_생성된_상품_ID_추출(ExtractableResponse<Response> response) {
        return response.jsonPath().getLong("id");
    }

    // 상품 재고 조회
    public static ExtractableResponse<Response> Admin_상품_재고_조회(Long productId) {
        ExtractableResponse<Response> response = ApiClientFactory.admin()
            .get("/admin/products/" + productId)
            .needAuth()
            .execute();

        assertThat(response.statusCode()).isEqualTo(200);
        return response;
    }

    // 재고 차감 검증
    public static void Admin_재고_차감_검증(ExtractableResponse<Response> response, int expectedStock) {
        int actualStock = response.jsonPath().getInt("stockQuantity");
        assertThat(actualStock).isEqualTo(expectedStock);
    }

    // 매출 기록 조회
    public static ExtractableResponse<Response> Admin_매출_기록_조회(Long productId) {
        ExtractableResponse<Response> response = ApiClientFactory.admin()
            .get("/admin/sales?productId=" + productId)
            .needAuth()
            .execute();

        assertThat(response.statusCode()).isEqualTo(200);
        return response;
    }

    // 매출 기록 존재 검증
    public static void Admin_매출_기록_존재_검증(ExtractableResponse<Response> response, Long productId, Map<String, String> purchaseData) {
        List<Map<String, Object>> salesRecords = response.jsonPath().getList("$");
        assertThat(salesRecords).isNotEmpty();

        // 최근 매출 기록 확인
        Map<String, Object> latestSale = salesRecords.get(0);
        assertThat(latestSale.get("productId")).isEqualTo(productId.intValue());
        assertThat(latestSale.get("quantity")).isEqualTo(Integer.parseInt(purchaseData.get("quantity")));
    }

    // Kiosk에서 조회한 상품 정보와 Admin에서 생성한 정보 일치 검증
    public static void 생성된_상품_정보_일치_검증(ExtractableResponse<Response> productListResponse, Long createdProductId, Map<String, String> expectedProductData) {
        List<Map<String, Object>> products = productListResponse.jsonPath().getList("$");

        Map<String, Object> foundProduct = products.stream()
            .filter(product -> ((Integer) product.get("id")).longValue() == createdProductId)
            .findFirst()
            .orElseThrow(() -> new AssertionError("생성된 상품을 Kiosk에서 찾을 수 없습니다."));

        assertThat(foundProduct.get("name")).isEqualTo(expectedProductData.get("name"));
        assertThat(foundProduct.get("price")).isEqualTo(Long.parseLong(expectedProductData.get("price")));
        assertThat(foundProduct.get("stockQuantity")).isEqualTo(Integer.parseInt(expectedProductData.get("stockQuantity")));
        assertThat(foundProduct.get("productType")).isEqualTo(expectedProductData.get("productType"));
    }

    // 상품 등록 성공 검증
    public static void Admin_상품_등록_성공_검증(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getLong("id")).isNotNull();
    }

    // 상품 정보 수정
    public static ExtractableResponse<Response> Admin_상품_정보_수정(Long productId, Map<String, String> updateData) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", updateData.get("name"));
        requestBody.put("price", Long.parseLong(updateData.get("newPrice")));
        requestBody.put("stockQuantity", Integer.parseInt(updateData.get("newStock")));

        ExtractableResponse<Response> response = ApiClientFactory.admin()
            .put("/admin/products/" + productId)
            .body(requestBody)
            .needAuth()
            .execute();

        assertThat(response.statusCode()).isEqualTo(200);
        return response;
    }

    // 상품 수정 성공 검증
    public static void Admin_상품_수정_성공_검증(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(200);
    }

    // 수정된 상품 정보 반영 검증
    public static void 수정된_상품_정보_반영_검증(ExtractableResponse<Response> productListResponse, Long productId, Map<String, String> expectedData) {
        List<Map<String, Object>> products = productListResponse.jsonPath().getList("$");

        Map<String, Object> foundProduct = products.stream()
            .filter(product -> ((Integer) product.get("id")).longValue() == productId)
            .findFirst()
            .orElseThrow(() -> new AssertionError("상품을 찾을 수 없습니다."));

        assertThat(foundProduct.get("name")).isEqualTo(expectedData.get("name"));
        assertThat(foundProduct.get("price")).isEqualTo(Long.parseLong(expectedData.get("expectedPrice")));
        assertThat(foundProduct.get("stockQuantity")).isEqualTo(Integer.parseInt(expectedData.get("expectedStock")));
    }

    // 예약 목록 조회
    public static ExtractableResponse<Response> Admin_예약_목록_조회() {
        ExtractableResponse<Response> response = ApiClientFactory.admin()
            .get("/admin/reservations")
            .needAuth()
            .execute();

        assertThat(response.statusCode()).isEqualTo(200);
        return response;
    }

    // 예약 목록 검증
    public static void Admin_예약_목록_검증(ExtractableResponse<Response> response, Long reservationId, Map<String, String> expectedData) {
        List<Map<String, Object>> reservations = response.jsonPath().getList("$");

        Map<String, Object> foundReservation = reservations.stream()
            .filter(reservation -> ((Integer) reservation.get("id")).longValue() == reservationId)
            .findFirst()
            .orElseThrow(() -> new AssertionError("예약을 찾을 수 없습니다."));

        assertThat(foundReservation.get("customerName")).isEqualTo(expectedData.get("customerName"));
        assertThat(foundReservation.get("siteName")).isEqualTo(expectedData.get("siteName"));
        assertThat(foundReservation.get("status")).isEqualTo(expectedData.get("status"));
    }

    // 예약 상태 변경
    public static ExtractableResponse<Response> Admin_예약_상태_변경(Long reservationId, Map<String, String> changeData) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("status", changeData.get("newStatus"));

        ExtractableResponse<Response> response = ApiClientFactory.admin()
            .patch("/admin/reservations/" + reservationId + "/status")
            .body(requestBody)
            .needAuth()
            .execute();

        assertThat(response.statusCode()).isEqualTo(200);
        return response;
    }

    // 예약 상태 변경 성공 검증
    public static void Admin_예약_상태_변경_성공_검증(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(200);
    }

    // 인증 요청
    public static ExtractableResponse<Response> Admin_인증_요청(Map<String, String> credentials) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", credentials.get("username"));
        requestBody.put("password", credentials.get("password"));

        ExtractableResponse<Response> response = ApiClientFactory.admin()
            .post("/admin/auth/login")
            .body(requestBody)
            .execute();

        assertThat(response.statusCode()).isEqualTo(200);
        return response;
    }

    // 인증 성공 검증
    public static void Admin_인증_성공_검증(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("accessToken")).isNotNull();
        assertThat(response.jsonPath().getString("tokenType")).isEqualTo("Bearer");
    }

    // 매출 기록 존재 검증 (오버로드)
    public static void Admin_매출_기록_존재_검증(ExtractableResponse<Response> response, Long productId) {
        List<Map<String, Object>> salesRecords = response.jsonPath().getList("$");
        assertThat(salesRecords).isNotEmpty();

        Map<String, Object> latestSale = salesRecords.get(0);
        assertThat(latestSale.get("productId")).isEqualTo(productId.intValue());
    }

    // 상품 정보 수정 시도 (경계값 처리용)
    public static ExtractableResponse<Response> Admin_상품_정보_수정_시도(Long productId, Map<String, String> updateData) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", updateData.get("name"));
        requestBody.put("price", Long.parseLong(updateData.get("newPrice")));
        requestBody.put("stockQuantity", Integer.parseInt(updateData.get("newStock")));

        return ApiClientFactory.admin()
            .put("/admin/products/" + productId)
            .body(requestBody)
            .needAuth()
            .execute();
    }
}