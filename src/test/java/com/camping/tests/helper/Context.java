package com.camping.tests.helper;

import io.restassured.response.Response;

/**
 * 테스트 전역 공용 컨텍스트
 */
public class Context {
    
    public static String kioskBaseUrl;
    public static String adminBaseUrl;
    public static String reservationBaseUrl;

    public static Response lastResponse;
    public static long requestStartTime;

    // 인증 관련 정보
    public static String authToken;
    
    public static void reset() {
        lastResponse = null;
        requestStartTime = 0;
        authToken = null;
    }
}
