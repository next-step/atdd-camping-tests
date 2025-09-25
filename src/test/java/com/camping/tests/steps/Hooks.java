package com.camping.tests.steps;

import com.camping.tests.support.helper.ServiceContext;
import com.camping.tests.support.helper.ServiceType;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Hooks {
    private static final Logger log = LoggerFactory.getLogger(Hooks.class);

    @Before
    public void beforeScenario() {
        ServiceContext.initializeRequestSpec(ServiceType.ADMIN);
        ServiceContext.initializeRequestSpec(ServiceType.KIOSK);
        ServiceContext.initializeRequestSpec(ServiceType.RESERVATION);
    }

    @BeforeAll
    public static void initAccessToken() {
        log.info("로그인 시도중...");
        Map<String, String> params = Map.of("username", "admin", "password", "admin123");
        String adminAccessToken = requestAdminLogin(params);
        ServiceContext.setAccessToken(ServiceType.ADMIN, adminAccessToken);
        ServiceContext.setAccessToken(ServiceType.KIOSK, adminAccessToken);
        log.info("로그인 완료");
    }

    private static String requestAdminLogin(Map<String, String> params) {
        ExtractableResponse<Response> response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(params)
                .when()
                .post(ServiceType.ADMIN.getBaseUrl() + "/auth/login")
                .then()
                .statusCode(200)
                .extract();

        return response.jsonPath().get("accessToken");
    }
}
