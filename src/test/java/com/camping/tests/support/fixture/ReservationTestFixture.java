package com.camping.tests.support.fixture;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import com.camping.tests.support.client.ApiClientFactory;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservationTestFixture {

    // 캠프사이트 생성
    public static ExtractableResponse<Response> Reservation_캠프사이트_생성(Map<String, String> siteData) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("siteName", siteData.get("siteName"));
        requestBody.put("maxPeople", Integer.parseInt(siteData.get("maxPeople")));
        requestBody.put("pricePerNight", Long.parseLong(siteData.get("pricePerNight")));

        ExtractableResponse<Response> response = ApiClientFactory.reservation()
            .post("/api/sites")
            .body(requestBody)
            .execute();

        assertThat(response.statusCode()).isEqualTo(201);
        return response;
    }

    // 예약 생성
    public static ExtractableResponse<Response> Reservation_예약_생성(Map<String, String> reservationData) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("siteName", reservationData.get("siteName"));
        requestBody.put("checkIn", reservationData.get("checkIn"));
        requestBody.put("checkOut", reservationData.get("checkOut"));
        requestBody.put("guestCount", Integer.parseInt(reservationData.get("guestCount")));
        requestBody.put("customerName", reservationData.get("customerName"));
        requestBody.put("phone", reservationData.get("phone"));

        ExtractableResponse<Response> response = ApiClientFactory.reservation()
            .post("/api/reservations")
            .body(requestBody)
            .execute();

        assertThat(response.statusCode()).isEqualTo(201);
        return response;
    }

    // 생성된 예약 ID 추출
    public static Long Reservation_생성된_예약_ID_추출(ExtractableResponse<Response> response) {
        return response.jsonPath().getLong("id");
    }

    // 예약 생성 성공 검증
    public static void Reservation_예약_생성_성공_검증(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getLong("id")).isNotNull();
        assertThat(response.jsonPath().getString("status")).isEqualTo("CONFIRMED");
    }

    // 사이트 가용성 확인
    public static ExtractableResponse<Response> Reservation_사이트_가용성_확인(String siteName, String checkDate) {
        ExtractableResponse<Response> response = ApiClientFactory.reservation()
            .get("/api/sites/" + siteName + "/availability?date=" + checkDate)
            .execute();

        assertThat(response.statusCode()).isEqualTo(200);
        return response;
    }

    // 가용성 검증
    public static void Reservation_가용성_불가능_검증(ExtractableResponse<Response> response) {
        assertThat(response.jsonPath().getBoolean("available")).isFalse();
    }

    public static void Reservation_가용성_가능_검증(ExtractableResponse<Response> response) {
        assertThat(response.jsonPath().getBoolean("available")).isTrue();
    }

    // 예약 생성 시도 (실패 가능)
    public static ExtractableResponse<Response> Reservation_예약_생성_시도(Map<String, String> reservationData) {
        Map<String, Object> requestBody = new HashMap<>();
        if (reservationData.containsKey("siteId")) {
            requestBody.put("siteId", Long.parseLong(reservationData.get("siteId")));
        } else {
            requestBody.put("siteName", reservationData.get("siteName"));
        }
        requestBody.put("checkIn", reservationData.get("checkIn"));
        requestBody.put("checkOut", reservationData.get("checkOut"));
        requestBody.put("guestCount", Integer.parseInt(reservationData.get("guestCount")));
        requestBody.put("customerName", reservationData.get("customerName"));

        return ApiClientFactory.reservation()
            .post("/api/reservations")
            .body(requestBody)
            .execute();
    }

    // 생성된 사이트 ID 추출
    public static Long Reservation_생성된_사이트_ID_추출(ExtractableResponse<Response> response) {
        return response.jsonPath().getLong("id");
    }

    // 예약 상태 변경
    public static ExtractableResponse<Response> Reservation_예약_상태_변경(Long reservationId, Map<String, String> changeData) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("status", changeData.get("newStatus"));

        ExtractableResponse<Response> response = ApiClientFactory.reservation()
            .patch("/api/reservations/" + reservationId + "/status")
            .body(requestBody)
            .execute();

        assertThat(response.statusCode()).isEqualTo(200);
        return response;
    }

    // 예약 상태 변경 성공 검증
    public static void Reservation_예약_상태_변경_성공_검증(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(200);
    }
}