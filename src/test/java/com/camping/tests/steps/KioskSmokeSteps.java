package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KioskSmokeSteps {

    private static final String KIOSK_BASE_URL =
            System.getenv("KIOSK_BASE_URL") != null
                    ? System.getenv("KIOSK_BASE_URL")
                    : "http://localhost:18081";


    @Then("kiosk 헬스 체크가 200 응답을 반환한다")
    public void kiosk_헬스_체크가_200_응답을_반환한다() {
        Response response = RestAssured
                .given().baseUri(KIOSK_BASE_URL)
                .when().get("/health");

        assertEquals(200, response.statusCode());
        assertEquals("OK", response.getBody().asString());
    }
}
