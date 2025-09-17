package com.camping.tests.steps;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class SmokeTestSteps {

    private final static String KIOSK_BASE_URL = System.getenv().getOrDefault("KIOSK_BASE_URL", "http://localhost:8080");

    private Response kioskServiceResponse;

    @When("kiosk 서비스에 요청을 보낸다")
    public void kiosk_서비스에_요청을_보낸다() {

        kioskServiceResponse = given()
            .when().get(KIOSK_BASE_URL)
            .thenReturn();
    }

    @Then("200 응답을 받는다")
    public void _200_응답을_받는다() {
        assertThat(kioskServiceResponse.statusCode()).isEqualTo(200);
    }
}
