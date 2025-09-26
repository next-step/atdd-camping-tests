package com.camping.tests.steps;

import com.camping.tests.helper.Context;
import io.cucumber.java.Before;
import io.restassured.RestAssured;

import static com.camping.tests.helper.BaseUrl.*;
import static com.camping.tests.helper.Context.*;

/**
 * 키오스크 테스트 설정 헬퍼
 */
public class Hooks {

    /**
     * RestAssured 기본 설정을 초기화합니다.
     */
    private static void initializeRestAssured() {
        kioskBaseUrl = KIOSK_URL;
        adminBaseUrl = ADMIN_URL;
        reservationBaseUrl = RESERVATION_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Before
    public void setUp() {
        if (kioskBaseUrl == null || adminBaseUrl == null || reservationBaseUrl == null) {
            initializeRestAssured();
        }
        Context.reset();
    }
}
