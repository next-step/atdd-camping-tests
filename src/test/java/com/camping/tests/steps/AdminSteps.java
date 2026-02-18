package com.camping.tests.steps;

import io.cucumber.java.ko.만약;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class AdminSteps {
    private final CommonContext context;

    public AdminSteps(CommonContext context) {
        this.context = context;
    }

    @만약("관리자 계정으로 로그인되어 있다")
    public void 관리자_계정으로_로그인되어_있다() {
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
                        .post(context.serviceUrl("admin") + "/auth/login")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        context.setAuthToken(response.jsonPath().getString("accessToken"));
    }
}
