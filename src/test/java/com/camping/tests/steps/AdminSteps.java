package com.camping.tests.steps;

import com.camping.tests.helpers.ApiTestHelper;
import com.camping.tests.helpers.ContextHelper;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class AdminSteps {

    private static final String AUTH_TOKEN_KEY = "authToken";

    @When("관리자로 로그인한다")
    public void 관리자로_로그인한다() {
        String baseUrl = ApiTestHelper.resolveBaseUrl("ADMIN_BASE_URL", "http://localhost:18082");
        String loginUrl = ApiTestHelper.buildUrl(baseUrl, "/auth/login");

        Response response = RestAssured.given()
                .contentType("application/json")
                .body("{\"username\":\"admin\",\"password\":\"admin123\"}")
                .when()
                .post(loginUrl);

        String token = response.jsonPath().getString("accessToken");
        ContextHelper.set(AUTH_TOKEN_KEY, token);
    }

}
