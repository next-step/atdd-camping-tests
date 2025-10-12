package com.camping.tests.helpers;

import com.camping.tests.context.CommonContextHolder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class AdminApiHelper {
    private static final String ADMIN_BASE_URL = System.getProperty("ADMIN_BASE_URL");

    public static Response getAllReservations() {
        String adminToken = CommonContextHolder.getInstance().getAdminToken();

        return given()
                .cookie("AUTH_TOKEN", adminToken)
                .when()
                .get(ADMIN_BASE_URL + "/admin/reservations");
    }

    public static Response updateReservationStatus(String reservationId, String status) {
        String adminToken = CommonContextHolder.getInstance().getAdminToken();

        return given()
                .cookie("AUTH_TOKEN", adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("status", status))
                .when()
                .patch(ADMIN_BASE_URL + "/admin/reservations/" + reservationId + "/status");
    }

    public static Response getSalesRecords() {
        String adminToken = CommonContextHolder.getInstance().getAdminToken();

        return given()
                .cookie("AUTH_TOKEN", adminToken)
                .when()
                .get(ADMIN_BASE_URL + "/api/sales");
    }
}
