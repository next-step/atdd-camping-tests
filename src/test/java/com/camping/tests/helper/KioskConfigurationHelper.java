package com.camping.tests.helper;

import io.restassured.RestAssured;

/**
 * 키오스크 테스트 설정 헬퍼
 */
public class KioskConfigurationHelper {
    
    /**
     * 환경변수 또는 시스템 프로퍼티에서 키오스크 베이스 URL을 가져옵니다.
     * 우선순위: 환경변수 > 시스템 프로퍼티 > 기본값
     */
    public static String getKioskBaseUrl() {
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
     * RestAssured 기본 설정을 초기화합니다.
     */
    public static void initializeRestAssured() {
        KioskContext.kioskBaseUrl = getKioskBaseUrl();
        RestAssured.baseURI = KioskContext.kioskBaseUrl;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        System.out.println("🔍 키오스크 베이스 URL: " + KioskContext.kioskBaseUrl);
    }
}
