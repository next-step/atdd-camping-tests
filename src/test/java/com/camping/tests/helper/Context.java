package com.camping.tests.helper;

import io.restassured.response.Response;

import java.util.Map;

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
    public static Map<String, String> authCookies;
    
    public static void reset() {
        lastResponse = null;
        requestStartTime = 0;
        authToken = null;
        authCookies = null;
    }
}
