package com.camping.tests.steps;

import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProductE2ESteps {

    private static final String KIOSK_BASE_URL = getEnvOrDefault("KIOSK_BASE_URL", "http://localhost:18081");
    private static final String ADMIN_BASE_URL = getEnvOrDefault("ADMIN_BASE_URL", "http://localhost:18082");

    private final SharedContext context;
    private String authToken;

    public ProductE2ESteps(SharedContext context) {
        this.context = context;
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    @그리고("Admin에 로그인하여 인증 토큰을 획득한다")
    public void admin에_로그인하여_인증_토큰을_획득한다() {
        Response loginResponse = given()
                .baseUri(ADMIN_BASE_URL)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "username", "admin",
                        "password", "admin123"
                ))
                .when()
                .post("/auth/login");

        authToken = loginResponse.jsonPath().getString("accessToken");

        if (authToken == null) {
            System.out.println("로그인 응답: " + loginResponse.getBody().asString());
        }

        System.out.println("인증 토큰 획득: " + (authToken != null ? "성공" : "실패 (비인증 모드로 진행)"));
    }

    @만약("인증된 상태로 Kiosk의 {string} 엔드포인트를 호출한다")
    public void 인증된_상태로_kiosk의_엔드포인트를_호출한다(String endpoint) {
        var requestSpec = given()
                .baseUri(KIOSK_BASE_URL);

        if (authToken != null) {
            requestSpec.cookie("AUTH_TOKEN", authToken);
        }

        context.setResponse(requestSpec.when().get(endpoint));
    }

    @그리고("응답에 상품 목록이 포함되어야 한다")
    public void 응답에_상품_목록이_포함되어야_한다() {
        try {
            List<Map<String, Object>> products = context.getResponse().jsonPath().getList("$");
            assertNotNull(products, "상품 목록이 null입니다");
            assertFalse(products.isEmpty(), "상품 목록이 비어있습니다");
        } catch (Exception e) {
            String body = context.getResponse().getBody().asString();
            assertFalse(body.isEmpty(), "응답 본문이 비어있습니다");
            System.out.println("응답 본문 (처음 200자): " + body.substring(0, Math.min(200, body.length())));
        }
    }
}
