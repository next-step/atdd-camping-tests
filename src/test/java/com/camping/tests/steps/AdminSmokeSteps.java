package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdminSmokeSteps {

    private static final String ADMIN_BASE_URL =
            System.getenv("ADMIN_BASE_URL") != null
                    ? System.getenv("ADMIN_BASE_URL")
                    : "http://localhost:18082";

    @Then("요청시 admin이 성공 응답을 반환한다")
    public void 요청시_admin이_성공_응답을_반환한다() {
        Response response = RestAssured
                .given().baseUri(ADMIN_BASE_URL)
                .when().get("/health");

        assertEquals(200, response.statusCode());
        assertEquals("OK", response.getBody().asString());
    }
}
