package com.camping.tests.steps.admin;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminReservationTestSteps {

    public static Response 모든_예약을_조회한다() {
        return AdminClient.given()
            .when().get("/admin/reservations")
            .thenReturn();
    }

    public static Response 예약_상태를_변경한다(Long reservationId, String status) {
        return AdminClient.given()
            .body(Map.of("status", status))
            .when().patch("/admin/reservations/" + reservationId + "/status")
            .thenReturn();
    }

    public static void 예약_상태_변경이_성공한다(Response response) {
        response.then().statusCode(HttpStatus.SC_OK);
    }

    public static void 예약_상태가_변경되었다(Response response, String expectedStatus) {
        response.then().statusCode(HttpStatus.SC_OK);

        Map<String, Object> responseBody = response.as(Map.class);
        String actualStatus = (String) responseBody.get("status");

        assertThat(actualStatus).isEqualTo(expectedStatus);
    }

    public static Long 첫번째_예약의_ID를_가져온다(Response response) {
        response.then().statusCode(HttpStatus.SC_OK);

        Object[] reservations = response.as(Object[].class);
        assertThat(reservations).isNotEmpty();

        Map<String, Object> firstReservation = (Map<String, Object>) reservations[0];
        return ((Number) firstReservation.get("id")).longValue();
    }
}