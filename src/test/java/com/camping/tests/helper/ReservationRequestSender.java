
package com.camping.tests.helper;

import io.restassured.response.Response;

/**
 * 관리자 시스템 API 요청 전송 유틸리티
 */
public class ReservationRequestSender {

    public static Response get(String endpoint) {
        return RequestSender.get(Context.reservationBaseUrl, endpoint);
    }
}
