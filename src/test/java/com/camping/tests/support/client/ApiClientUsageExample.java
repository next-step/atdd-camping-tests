package com.camping.tests.support.client;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.util.Map;

public class ApiClientUsageExample {

    public void fluentApiUsageExamples() {

        // 1. 기본 사용법 (인증 없음)
        ExtractableResponse<Response> basicGet = ApiClientFactory.kiosk()
                .get("/api/products")
                .execute();

        // 2. 기본 토큰으로 인증 (ServiceContext에서 설정된 토큰 사용)
        ExtractableResponse<Response> authenticatedGet = ApiClientFactory.admin()
                .get("/api/users")
                .needAuth()
                .execute();

        // 3. 커스텀 토큰으로 인증 override
        ExtractableResponse<Response> customTokenGet = ApiClientFactory.kiosk()
                .get("/api/products")
                .accessToken("custom-jwt-token-here")
                .execute();

        // 4. POST 요청 with body
        Map<String, Object> userData = Map.of(
                "name", "테스트 사용자",
                "email", "test@example.com"
        );

        ExtractableResponse<Response> postWithBody = ApiClientFactory.admin()
                .post("/api/users")
                .body(userData)
                .needAuth()
                .execute();

        // 5. POST 요청 with custom token and body
        Map<String, Object> reservationData = Map.of(
                "productId", 1,
                "quantity", 2,
                "date", "2024-01-15"
        );

        ExtractableResponse<Response> customTokenPost = ApiClientFactory.reservation()
                .post("/api/reservations")
                .body(reservationData)
                .accessToken("special-reservation-token")
                .execute();

        // 6. PATCH 요청 with custom token
        Map<String, String> statusUpdate = Map.of("status", "APPROVED");

        ExtractableResponse<Response> patchWithCustomToken = ApiClientFactory.admin()
                .patch("/api/reservations/123")
                .body(statusUpdate)
                .accessToken("admin-override-token")
                .execute();

        // 7. 체이닝 없이 바로 실행 (기본값 사용)
        ExtractableResponse<Response> simpleGet = ApiClientFactory.kiosk()
                .get("/api/health")
                .execute();

        // 8. 기존 방식과의 비교
        // 기존 방식 (여전히 지원됨)
        ExtractableResponse<Response> oldStyle = ApiClientFactory.kiosk()
                .getDirectly("/api/products", true);

        // 새로운 Fluent 방식 (권장)
        ExtractableResponse<Response> newStyle = ApiClientFactory.kiosk()
                .get("/api/products")
                .needAuth()
                .execute();
    }

    public void testFixtureUsageExample() {
        // TestFixture에서의 활용 예시

        // 1. 기본 토큰으로 상품 목록 조회
        ExtractableResponse<Response> products = ApiClientFactory.kiosk()
                .get("/api/products")
                .needAuth()
                .execute();

        // 2. 특정 관리자 토큰으로 사용자 생성
        String managerToken = "manager-specific-token";
        ExtractableResponse<Response> newUser = ApiClientFactory.admin()
                .post("/api/users")
                .body(Map.of("name", "매니저가 생성한 사용자"))
                .accessToken(managerToken)
                .execute();

        // 3. 서로 다른 서비스, 서로 다른 토큰으로 크로스 검증
        String kioskToken = "kiosk-service-token";
        String adminToken = "admin-service-token";

        // 키오스크에서 예약 생성
        ExtractableResponse<Response> reservation = ApiClientFactory.kiosk()
                .post("/api/reservations")
                .body(Map.of("productId", 1, "quantity", 2))
                .accessToken(kioskToken)
                .execute();

        long reservationId = reservation.jsonPath().getLong("id");

        // 관리자에서 예약 승인
        ExtractableResponse<Response> approval = ApiClientFactory.admin()
                .patch("/api/admin/reservations/" + reservationId)
                .body(Map.of("status", "APPROVED"))
                .accessToken(adminToken)
                .execute();
    }
}