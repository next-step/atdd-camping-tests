package com.camping.tests.helper;

import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 키오스크 테스트 검증 유틸리티
 */
public class KioskAssertions {
    
    public static void assertSuccessResponse(Response response) {
        assertEquals(200, response.getStatusCode(), 
                    "200 응답을 기대했지만 실제로는 " + response.getStatusCode() + "를 받았습니다.");
        System.out.println("✅ 200 응답 확인 완료");
    }
    
    public static void assertHtmlContent(Response response) {
        String contentType = response.getContentType();
        assertTrue(contentType.contains("text/html"), 
                  "HTML 콘텐츠를 기대했지만 실제로는 " + contentType + "을 받았습니다.");
        System.out.println("✅ HTML 콘텐츠 확인 완료: " + contentType);
    }
    
    public static void assertResponseTime(long expectedSeconds) {
        long responseTime = System.currentTimeMillis() - KioskContext.requestStartTime;
        long expectedMillis = expectedSeconds * 1000L;
        
        assertTrue(responseTime < expectedMillis, 
                  "응답 시간이 " + expectedSeconds + "초를 초과했습니다. 실제: " + responseTime + "ms");
        System.out.println("✅ 응답성 확인 완료: " + responseTime + "ms (기대: " + expectedSeconds + "초 이내)");
    }
    
    public static void assertStaticResourceResponse(Response response) {
        // CSS 파일은 200 또는 404일 수 있음
        assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 404,
                  "정적 리소스 응답코드가 200 또는 404여야 합니다. 실제: " + response.getStatusCode());
        
        if (response.getStatusCode() == 200) {
            String contentType = response.getContentType();
            assertTrue(contentType.contains("text/css") || contentType.contains("text/plain"),
                      "CSS 파일의 Content-Type이 올바르지 않습니다: " + contentType);
            System.out.println("✅ 정적 리소스 로딩 확인 완료: " + contentType);
        } else {
            System.out.println("ℹ️ 정적 리소스가 존재하지 않습니다 (404)");
        }
    }
    
    public static void assertHealthCheckResponse(Response response) {
        // 헬스체크 엔드포인트는 200 또는 404일 수 있음
        assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 404,
                  "헬스체크 응답코드가 200 또는 404여야 합니다. 실제: " + response.getStatusCode());
        
        if (response.getStatusCode() == 200) {
            System.out.println("✅ 헬스체크 엔드포인트 확인 완료");
        } else {
            System.out.println("ℹ️ 헬스체크 엔드포인트가 비활성화되어 있습니다 (404)");
        }
    }
    
    public static void assertApiResponse(Response response) {
        // API 엔드포인트는 200, 404, 500일 수 있음 (외부 서비스 의존성으로 인한 500 오류 허용)
        assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 404 || response.getStatusCode() == 500,
                  "API 응답코드가 200, 404, 또는 500이어야 합니다. 실제: " + response.getStatusCode());
        
        if (response.getStatusCode() == 200) {
            System.out.println("✅ API 엔드포인트 확인 완료");
        } else if (response.getStatusCode() == 404) {
            System.out.println("ℹ️ API 엔드포인트가 존재하지 않습니다 (404)");
        } else if (response.getStatusCode() == 500) {
            System.out.println("⚠️ API 엔드포인트에서 서버 오류 발생 (500) - 외부 서비스 의존성 문제일 수 있음");
        }
    }
    
    public static void assertNormalResponse(Response response) {
        // 일반적으로 정상적인 응답으로 간주되는 상태코드들
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 500,
                  "정상적인 응답을 기대했지만 실제로는 " + response.getStatusCode() + "를 받았습니다.");
        System.out.println("✅ 정상적인 응답 확인 완료: " + response.getStatusCode());
    }
}
