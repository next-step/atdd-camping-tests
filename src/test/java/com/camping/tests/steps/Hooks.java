package com.camping.tests.steps;

import com.camping.tests.helper.Context;
import io.cucumber.java.Before;
import io.restassured.RestAssured;

/**
 * 캠핑 시스템 테스트 설정 헬퍼
 */
public class Hooks {

    /**
     * RestAssured 기본 설정을 초기화합니다.
     */
    public static void initializeRestAssured() {
        Context.kioskBaseUrl = getKioskBaseUrl();
        Context.adminBaseUrl = getAdminBaseUrl();
        Context.reservationBaseUrl = getReservationBaseUrl();

        // 기본 키오스크 URL을 RestAssured 기본값으로 설정 (하위 호환성)
        RestAssured.baseURI = Context.kioskBaseUrl;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Before
    public void setUp() {
        if (Context.kioskBaseUrl == null) {
            Hooks.initializeRestAssured();
        }
        Context.reset();
    }

    /**
     * 환경변수 또는 시스템 프로퍼티에서 키오스크 베이스 URL을 가져옵니다.
     * 우선순위: 환경변수 > 시스템 프로퍼티 > 기본값
     */
    private static String getKioskBaseUrl() {
        // 1. 환경변수에서 확인
        String envUrl = System.getenv("KIOSK_BASE_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return envUrl.trim();
        }

        // 2. 시스템 프로퍼티에서 확인
        String propUrl = System.getProperty("kiosk.base.url");
        if (propUrl != null && !propUrl.trim().isEmpty()) {
            return propUrl.trim();
        }

        // 3. 기본값
        return "http://localhost:8080";
    }

    /**
     * 환경변수 또는 시스템 프로퍼티에서 관리자 시스템 베이스 URL을 가져옵니다.
     * 우선순위: 환경변수 > 시스템 프로퍼티 > 기본값
     */
    private static String getAdminBaseUrl() {
        // 1. 환경변수에서 확인
        String envUrl = System.getenv("ADMIN_BASE_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return envUrl.trim();
        }

        // 2. 시스템 프로퍼티에서 확인
        String propUrl = System.getProperty("admin.base.url");
        if (propUrl != null && !propUrl.trim().isEmpty()) {
            return propUrl.trim();
        }

        // 3. 기본값
        return "http://localhost:8081";
    }

    /**
     * 환경변수 또는 시스템 프로퍼티에서 예약 시스템 베이스 URL을 가져옵니다.
     * 우선순위: 환경변수 > 시스템 프로퍼티 > 기본값
     */
    private static String getReservationBaseUrl() {
        // 1. 환경변수에서 확인
        String envUrl = System.getenv("RESERVATION_BASE_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return envUrl.trim();
        }

        // 2. 시스템 프로퍼티에서 확인
        String propUrl = System.getProperty("reservation.base.url");
        if (propUrl != null && !propUrl.trim().isEmpty()) {
            return propUrl.trim();
        }

        // 3. 기본값
        return "http://localhost:8082";
    }
}
