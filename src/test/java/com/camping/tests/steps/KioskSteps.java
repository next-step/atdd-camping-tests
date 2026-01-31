package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class KioskSteps {

    private final String kioskBaseUrl;
    private Response response;

    public KioskSteps() {
        this.kioskBaseUrl = Optional.ofNullable(System.getenv("KIOSK_BASE_URL"))
                .orElse("http://localhost:18081");
    }

    @When("{string}에 요청을 보낸다")
    public void 요청을_보낸다(String path) {
        response = RestAssured.given()
                .when()
                .get(kioskBaseUrl + path);
    }

    @Then("성공 응답을 받는다")
    public void 성공_응답을_받는다() {
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Then("응답 본문은 {string}이다")
    public void 응답_본문을_확인한다(String body) {
        assertThat(response.getBody().asString()).isEqualTo(body);
    }
}


