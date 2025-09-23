package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class SampleSteps {

    private Response response;

    @When("{string}에 요청을 보낸다")
    public void 요청을보낸다(String url) {
        response = RestAssured.given()
                .when()
                .get(url);
    }

    @Then("성공 응답을 받는다")
    public void 성공응답을받는다() {
        response.then().statusCode(200);
    }
}
