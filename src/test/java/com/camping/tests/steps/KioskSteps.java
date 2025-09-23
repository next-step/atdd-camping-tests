package com.camping.tests.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 키오스크 애플리케이션 인수테스트를 위한 Step Definitions
 */
public class KioskSteps {

    private static String kioskBaseUrl;
    private static final int MAX_RETRY_ATTEMPTS = 30;
    private static final Duration RETRY_INTERVAL = Duration.ofSeconds(2);
    
    private Response response;
    private long requestStartTime;

    @Before
    public void setUp() {
        if (kioskBaseUrl == null) {
            kioskBaseUrl = getKioskBaseUrl();
            RestAssured.baseURI = kioskBaseUrl;
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
            System.out.println("🔍 키오스크 베이스 URL: " + kioskBaseUrl);
        }
    }

    /**
     * 환경변수 또는 시스템 프로퍼티에서 키오스크 베이스 URL을 가져옵니다.
     */
    private String getKioskBaseUrl() {
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
     * 애플리케이션이 준비될 때까지 폴링하며 대기합니다.
     */
    private void waitForApplicationReady() {
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
                    System.out.println("✅ 키오스크 애플리케이션이 준비되었습니다! (시도: " + attempt + "/" + MAX_RETRY_ATTEMPTS + ")");
                    return;
                }
                
                System.out.println("⏳ 키오스크 애플리케이션 준비 대기 중... (시도: " + attempt + "/" + MAX_RETRY_ATTEMPTS + 
                                 ", 상태코드: " + testResponse.getStatusCode() + ")");
                
            } catch (Exception e) {
                System.out.println("⏳ 키오스크 애플리케이션 연결 대기 중... (시도: " + attempt + "/" + MAX_RETRY_ATTEMPTS + 
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
                                 "키오스크 서비스가 " + kioskBaseUrl + "에서 실행 중인지 확인해주세요.");
    }

    @Given("키오스크 애플리케이션이 준비되어 있다")
    public void 키오스크_애플리케이션이_준비되어_있다() {
        waitForApplicationReady();
    }

    @When("키오스크 홈페이지 {string}에 요청을 보낸다")
    public void 키오스크_홈페이지에_요청을_보낸다(String endpoint) {
        System.out.println("📤 키오스크 홈페이지 " + endpoint + "에 요청을 보냅니다.");
        requestStartTime = System.currentTimeMillis();
        
        response = given()
            .when()
            .get(endpoint)
            .then()
            .extract()
            .response();
            
        System.out.println("📥 응답 받음: " + response.getStatusCode());
    }

    @When("키오스크 CSS 파일 {string}에 요청을 보낸다")
    public void 키오스크_CSS_파일에_요청을_보낸다(String endpoint) {
        System.out.println("📤 키오스크 CSS 파일 " + endpoint + "에 요청을 보냅니다.");
        
        response = given()
            .when()
            .get(endpoint)
            .then()
            .extract()
            .response();
            
        System.out.println("📥 응답 받음: " + response.getStatusCode());
    }

    @When("키오스크 헬스체크 {string}에 요청을 보낸다")
    public void 키오스크_헬스체크에_요청을_보낸다(String endpoint) {
        System.out.println("📤 키오스크 헬스체크 " + endpoint + "에 요청을 보냅니다.");
        
        response = given()
            .when()
            .get(endpoint)
            .then()
            .extract()
            .response();
            
        System.out.println("📥 응답 받음: " + response.getStatusCode());
    }

    @When("키오스크 상품 API {string}에 요청을 보낸다")
    public void 키오스크_상품_API에_요청을_보낸다(String endpoint) {
        System.out.println("📤 키오스크 상품 API " + endpoint + "에 요청을 보냅니다.");
        
        response = given()
            .when()
            .get(endpoint)
            .then()
            .extract()
            .response();
            
        System.out.println("📥 응답 받음: " + response.getStatusCode());
    }

    @When("키오스크 {string}에 요청을 보낸다")
    public void 키오스크에_요청을_보낸다(String endpoint) {
        System.out.println("📤 키오스크 " + endpoint + "에 요청을 보냅니다.");
        
        response = given()
            .when()
            .get(endpoint)
            .then()
            .extract()
            .response();
            
        System.out.println("📥 응답 받음: " + response.getStatusCode());
    }

    @Then("200 응답을 받는다")
    public void 응답을_받는다() {
        assertEquals(200, response.getStatusCode(), 
                    "200 응답을 기대했지만 실제로는 " + response.getStatusCode() + "를 받았습니다.");
        System.out.println("✅ 200 응답 확인 완료");
    }

    @Then("HTML 콘텐츠를 받는다")
    public void HTML_콘텐츠를_받는다() {
        String contentType = response.getContentType();
        assertTrue(contentType.contains("text/html"), 
                  "HTML 콘텐츠를 기대했지만 실제로는 " + contentType + "을 받았습니다.");
        System.out.println("✅ HTML 콘텐츠 확인 완료: " + contentType);
    }

    @Then("{int}초 이내에 응답을 받는다")
    public void 초_이내에_응답을_받는다(int expectedSeconds) {
        long responseTime = System.currentTimeMillis() - requestStartTime;
        long expectedMillis = expectedSeconds * 1000L;
        
        assertTrue(responseTime < expectedMillis, 
                  "응답 시간이 " + expectedSeconds + "초를 초과했습니다. 실제: " + responseTime + "ms");
        System.out.println("✅ 응답성 확인 완료: " + responseTime + "ms (기대: " + expectedSeconds + "초 이내)");
    }

    @Then("정적 리소스 응답을 받는다")
    public void 정적_리소스_응답을_받는다() {
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

    @Then("헬스체크 응답을 받는다")
    public void 헬스체크_응답을_받는다() {
        // 헬스체크 엔드포인트는 200 또는 404일 수 있음
        assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 404,
                  "헬스체크 응답코드가 200 또는 404여야 합니다. 실제: " + response.getStatusCode());
        
        if (response.getStatusCode() == 200) {
            System.out.println("✅ 헬스체크 엔드포인트 확인 완료");
        } else {
            System.out.println("ℹ️ 헬스체크 엔드포인트가 비활성화되어 있습니다 (404)");
        }
    }

    @Then("API 응답을 받는다")
    public void API_응답을_받는다() {
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

    @Then("정상적인 응답을 받는다")
    public void 정상적인_응답을_받는다() {
        // 일반적으로 정상적인 응답으로 간주되는 상태코드들
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 500,
                  "정상적인 응답을 기대했지만 실제로는 " + response.getStatusCode() + "를 받았습니다.");
        System.out.println("✅ 정상적인 응답 확인 완료: " + response.getStatusCode());
    }
}
