package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import static com.camping.tests.config.ExternalAPIConfig.키오스크_시스템_호스트;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
public class SampleSteps {
    private ExtractableResponse<Response> response;

    @When("키오스크에 요청을 보낸다")
    public void 요청을보낸다() {
        response = RestAssured.given()
                .baseUri(키오스크_시스템_호스트())
                .when()
                .get("/")
                .then()
                .extract();
    }

    @Then("성공 응답을 받는다")
    public void 성공응답을받는다() {
        assertThat(response.statusCode()).isEqualTo(200);
    }
}


