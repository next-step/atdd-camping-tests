package com.camping.tests.steps;

import com.camping.tests.api.ReservationApi;
import io.cucumber.java.en.When;

@SuppressWarnings("NonAsciiCharacters")
public class ReservationSteps {
    private final ReservationApi reservationApi;

    public ReservationSteps(ReservationApi reservationApi) {
        this.reservationApi = reservationApi;
    }

    @When("예약 시스템의 상태를 확인하면")
    public void 예약_시스템의_상태를_확인하면() {
        reservationApi.헬스_체크();
    }

}
