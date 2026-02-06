package com.camping.tests.steps;

import com.camping.tests.support.AdminClient;
import com.camping.tests.support.ReservationClient;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.조건;
import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.*;

public class ReservationSteps {

    private final ReservationClient reservationClient = new ReservationClient();
    private final AdminClient adminClient = new AdminClient();

    private Response reservationResponse;
    private long reservationId;
    private String confirmationCode;

    @조건("캠핑장 사이트 {string}에 예약을 생성한다")
    public void createReservation(String campSiteId) {
        reservationResponse = reservationClient.createReservation(campSiteId);
        int statusCode = reservationResponse.getStatusCode();
        assertTrue(statusCode >= 200 && statusCode < 300,
                "예약 생성 실패. 상태코드: " + statusCode + ", 응답: " + reservationResponse.getBody().asString());
        reservationId = reservationResponse.jsonPath().getLong("id");
        confirmationCode = reservationResponse.jsonPath().getString("confirmationCode");
    }

    @만약("예약 서비스에서 예약 상태를 조회한다")
    public void getReservationStatus() {
        reservationResponse = reservationClient.getReservation(reservationId);
    }

    @그러면("예약 상태는 {string}이어야 한다")
    public void verifyReservationStatus(String expectedStatus) {
        String actualStatus = reservationResponse.jsonPath().getString("status");
        assertEquals(expectedStatus, actualStatus,
                "예약 상태가 " + expectedStatus + "이어야 합니다. 실제: " + actualStatus);
    }

    @만약("관리자가 확인 코드로 예약을 조회한다")
    public void adminLookupByConfirmationCode() {
        reservationResponse = adminClient.lookupReservationByCode(confirmationCode);
    }

    @그러면("예약 정보가 조회되어야 한다")
    public void verifyReservationFound() {
        int statusCode = reservationResponse.getStatusCode();
        assertTrue(statusCode >= 200 && statusCode < 300,
                "예약 조회 실패. 상태코드: " + statusCode + ", 응답: " + reservationResponse.getBody().asString());
    }

    @만약("예약을 확인 코드로 취소한다")
    public void cancelReservation() {
        reservationResponse = reservationClient.cancelReservation(reservationId, confirmationCode);
    }

    @그러면("예약 취소가 성공해야 한다")
    public void verifyCancellationSuccess() {
        int statusCode = reservationResponse.getStatusCode();
        assertTrue(statusCode >= 200 && statusCode < 300,
                "예약 취소 실패. 상태코드: " + statusCode + ", 응답: " + reservationResponse.getBody().asString());
    }

    @그러면("관리자가 예약을 조회하면 상태가 {string}이어야 한다")
    public void adminVerifyReservationStatus(String expectedStatus) {
        Response adminResponse = adminClient.lookupReservationByCode(confirmationCode);
        String actualStatus = adminResponse.jsonPath().getString("status");
        assertEquals(expectedStatus, actualStatus,
                "관리자 조회 시 예약 상태가 " + expectedStatus + "이어야 합니다. 실제: " + actualStatus);
    }

    @만약("관리자가 해당 예약을 {string} 상태로 변경한다")
    public void adminUpdateReservationStatus(String newStatus) {
        reservationResponse = adminClient.updateReservationStatus(reservationId, newStatus);
        int statusCode = reservationResponse.getStatusCode();
        assertTrue(statusCode >= 200 && statusCode < 300,
                "예약 상태 변경 실패. 상태코드: " + statusCode + ", 응답: " + reservationResponse.getBody().asString());
    }

    @그리고("환불이 처리되어야 한다")
    public void verifyRefundProcessed() {
        // @wip: 환불 API가 아직 구현되지 않음
        throw new io.cucumber.java.PendingException("환불 API 미구현");
    }
}
