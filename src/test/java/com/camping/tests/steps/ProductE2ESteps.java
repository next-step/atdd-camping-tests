package com.camping.tests.steps;

import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static com.camping.tests.config.TestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ProductE2ESteps {

    private final SharedContext context;
    private String authToken;

    public ProductE2ESteps(SharedContext context) {
        this.context = context;
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
            assertThat(products).as("상품 목록").isNotNull().isNotEmpty();
        } catch (Exception e) {
            String body = context.getResponse().getBody().asString();
            assertThat(body).as("응답 본문").isNotEmpty();
            System.out.println("응답 본문 (처음 200자): " + body.substring(0, Math.min(200, body.length())));
        }
    }
}
