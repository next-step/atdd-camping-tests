package com.camping.tests.api;

import com.camping.tests.context.HttpContext;

import static com.camping.tests.config.TestEnvironment.kioskHost;
import static com.camping.tests.config.TestEnvironment.reservationHost;

@SuppressWarnings("NonAsciiCharacters")
public class ReservationApi extends BasicApi {

    public ReservationApi(HttpContext httpContext) {
        super(httpContext);
    }

    public void 헬스_체크() {
        get(reservationHost(), "/");
    }
}
