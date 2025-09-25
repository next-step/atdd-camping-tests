package com.camping.tests.helper;

import io.restassured.response.Response;

/**
 * 키오스크 테스트 전역 공용 컨텍스트
 */
public class Context {
    
    public static String kioskBaseUrl;
    public static Response lastResponse;
    public static long requestStartTime;
    
    public static void reset() {
        lastResponse = null;
        requestStartTime = 0;
    }
}
