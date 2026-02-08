package com.camping.tests.steps;

import io.cucumber.java.ko.만약;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class AdminSteps {
    private static final Map<String, String> SERVICE_URLS = Map.of(
            "kiosk", System.getProperty("kiosk.base.url",
                    System.getenv().getOrDefault("KIOSK_BASE_URL", "http://localhost:18080")),
            "admin", System.getProperty("admin.base.url",
                    System.getenv().getOrDefault("ADMIN_BASE_URL", "http://localhost:18081")),
            "reservation", System.getProperty("reservation.base.url",
                    System.getenv().getOrDefault("RESERVATION_BASE_URL", "http://localhost:18082"))
    );

    private final TestContext context;

    public AdminSteps(TestContext context) {
        this.context = context;
    }

    @만약("관리자 계정으로 로그인되어 있다")
    public void login() {
        Response response =
                given()
                        .contentType("application/json")
                        .body("""
                    {
                      "username": "admin",
                      "password": "admin123"
                    }
                        """)
                        .when()
                        .post(SERVICE_URLS.get("admin") + "/auth/login")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        // 쿠키 저장
//        context.setAuthCookies(response.getCookies());
        context.setAuthToken(response.jsonPath().getString("accessToken"));
    }
}
