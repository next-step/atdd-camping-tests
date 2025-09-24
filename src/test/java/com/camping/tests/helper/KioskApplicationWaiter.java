package com.camping.tests.helper;

import groovy.util.logging.Slf4j;
import io.restassured.response.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.Duration;

import static io.restassured.RestAssured.given;

/**
 * 키오스크 애플리케이션 준비 상태 대기 유틸리티
 */
@Slf4j
public class KioskApplicationWaiter {
    
    private static final int MAX_RETRY_ATTEMPTS = 30;
    private static final Duration RETRY_INTERVAL = Duration.ofSeconds(2);
    private static final Log log = LogFactory.getLog(KioskApplicationWaiter.class);

    /**
     * 애플리케이션이 준비될 때까지 폴링하며 대기합니다.
     */
    public static void waitForApplicationReady() {
        System.out.println("⏳ 키오스크 애플리케이션 준비 상태를 확인하는 중...");
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                Response testResponse = given()
                    .when()
                    .get("/")
                    .then()
                    .extract()
                    .response();
                
                if (testResponse.getStatusCode() == 200) {
                    return;
                }
                
            } catch (Exception e) {
                log.error("⏳ 키오스크 애플리케이션 연결 대기 중... (시도: " + attempt + "/" + MAX_RETRY_ATTEMPTS +
                        ", 오류: " + e.getMessage() + ")");
            }
            
            if (attempt < MAX_RETRY_ATTEMPTS) {
                try {
                    Thread.sleep(RETRY_INTERVAL.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("대기 중 인터럽트 발생", e);
                }
            }
        }
        
        throw new RuntimeException("키오스크 애플리케이션이 " + MAX_RETRY_ATTEMPTS + "번의 시도 후에도 준비되지 않았습니다. " +
                                 "키오스크 서비스가 " + KioskContext.kioskBaseUrl + "에서 실행 중인지 확인해주세요.");
    }
}
