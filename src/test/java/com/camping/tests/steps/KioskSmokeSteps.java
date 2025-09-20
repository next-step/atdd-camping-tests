package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KioskSmokeSteps {

    private Response response;

    @When("{string}에 요청을 보낸다")
    public void 요청을보낸다(String url) {
        response = given().when().get(url);
    }

    @Then("{int} 응답을 받는다")
    public void 응답을받는다(int expectedStatus) {
        assertEquals(expectedStatus, response.getStatusCode());
    }
}