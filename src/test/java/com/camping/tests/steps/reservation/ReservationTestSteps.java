package com.camping.tests.steps.reservation;

import com.camping.tests.steps.reservation.dto.ReservationRequest;
import com.camping.tests.steps.reservation.dto.ReservationResponse;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservationTestSteps {

    public static Response 예약을_생성한다(ReservationRequest request) {
        return ReservationClient.given()
            .body(request)
            .when().post("/api/reservations")
            .thenReturn();
    }

    public static void 예약_성공_확인(Response response) {
        response.then().statusCode(HttpStatus.SC_CREATED);
    }

    public static void 예약_확인_코드_생성_확인(Response response) {
        ReservationResponse reservation = response.as(ReservationResponse.class);
        assertThat(reservation.confirmationCode()).isNotNull();
        assertThat(reservation.confirmationCode()).isNotEmpty();
    }

    public static ReservationResponse 예약_정보를_가져온다(Response response) {
        return response.as(ReservationResponse.class);
    }
}