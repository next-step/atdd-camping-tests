package com.camping.tests.helpers;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ReservationApiHelper {
    private static final String RESERVATION_BASE_URL = System.getProperty("RESERVATION_BASE_URL");

    public static Response getAvailableSites(String date) {
        return given()
                .queryParam("date", date)
                .when()
                .get(RESERVATION_BASE_URL + "/api/sites/available");
    }

    public static Response createReservation(Integer siteNumber, String startDate, String endDate) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("siteNumber", "A-" + siteNumber);
        requestBody.put("customerName", "테스트 고객");
        requestBody.put("phoneNumber", "010-1234-5678");
        requestBody.put("startDate", startDate);
        requestBody.put("endDate", endDate);
        requestBody.put("numberOfPeople", 2);

        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(RESERVATION_BASE_URL + "/api/reservations");
    }

    public static Response getReservationById(String reservationId) {
        return given()
                .when()
                .get(RESERVATION_BASE_URL + "/api/reservations/" + reservationId);
    }

    public static Response getReservationByConfirmationCode(String confirmationCode) {
        return given()
                .queryParam("confirmationCode", confirmationCode)
                .when()
                .get(RESERVATION_BASE_URL + "/api/reservations");
    }

    public static Response cancelReservation(String reservationId, String confirmationCode) {
        return given()
                .queryParam("confirmationCode", confirmationCode)
                .when()
                .delete(RESERVATION_BASE_URL + "/api/reservations/" + reservationId);
    }
}
