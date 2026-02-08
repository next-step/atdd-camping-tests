package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleSteps {

    private static final String KIOSK_BASE_URL = System.getProperty(
            "kiosk.base.url",
            System.getenv().getOrDefault("KIOSK_BASE_URL", "http://localhost:18080")
    );

    private Response response;

    @When("{string}에 요청을 보낸다")
    public void 요청을보낸다(String path) {
        response = RestAssured.get(KIOSK_BASE_URL + path);
    }

    @Then("성공 응답을 받는다")
    public void 성공응답을받는다() {
        assertEquals(200, response.statusCode());
    }
}


