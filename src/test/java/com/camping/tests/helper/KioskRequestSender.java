package com.camping.tests.helper;

import io.restassured.response.Response;

/**
 * 키오스크 시스템 API 요청 전송 유틸리티
 */
public class KioskRequestSender {

    public static Response get(String endpoint) {
        return RequestSender.get(Context.kioskBaseUrl, endpoint);
    }
}
