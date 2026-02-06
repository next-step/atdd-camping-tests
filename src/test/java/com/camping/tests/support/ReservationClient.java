package com.camping.tests.support;

import com.camping.tests.config.TestConfig;
import com.camping.tests.dto.ReservationRequest;
import io.restassured.response.Response;

import static com.camping.tests.support.Endpoints.Reservation;

public class ReservationClient {

    private final ApiClient api;

    public ReservationClient() {
        this.api = new ApiClient(TestConfig.getReservationBaseUrl());
    }

    public Response createReservation(String campSiteId) {
        return api.post(Reservation.BASE, ReservationRequest.of(campSiteId));
    }

    public Response getReservation(long reservationId) {
        String path = String.format(Reservation.BY_ID, reservationId);
        return api.get(path);
    }

    public Response cancelReservation(long reservationId, String confirmationCode) {
        String path = String.format(Reservation.BY_ID, reservationId);
        return api.delete(path + "?confirmationCode=" + confirmationCode);
    }
}
