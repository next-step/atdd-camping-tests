package com.camping.tests.steps.admin;

import io.restassured.response.Response;
import java.util.Map;
import org.apache.http.HttpStatus;

public class AdminAuthTestSteps {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    public static Response 어드민으로_로그인이_되어있다() {
        return 로그인이_되어있다(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public static Response 로그인이_되어있다(String username, String password) {
        var 로그인_응답 = 로그인을_한다(username, password);
        로그인이_성공한다(로그인_응답);
        var authToken = 인증_토큰을_추출한다(로그인_응답);
        인증_토큰을_저장한다(authToken);
        return 로그인_응답;
    }

    public static Response 로그인을_한다(String username, String password) {
        return AdminClient.given()
            .body(Map.of(
                "username", username,
                "password", password
            ))
            .when().post("/auth/login")
            .thenReturn();
    }

    public static void 로그인이_성공한다(Response response) {
        response.then().statusCode(HttpStatus.SC_OK);
    }

    public static String 인증_토큰을_추출한다(Response response) {
        return response.cookie("AUTH_TOKEN");
    }

    public static void 인증_토큰을_저장한다(String authToken) {
        AdminClient.setAuthToken(authToken);
    }
}
