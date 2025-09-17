
package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;


import static io.restassured.RestAssured.given;

public class SmokeTestSteps {

    private Response response;
    private final String kioskBaseUrl;

    public SmokeTestSteps() {
        this.kioskBaseUrl = System.getProperty("KIOSK_BASE_URL", "http://localhost:8081");
    }

    @When("키오스크의 상태 확인 엔드포인트로 요청을 보내면")
    public void requestKioskHealthCheck() {
        response = given()
                .when()
                .get(kioskBaseUrl);
    }

    @Then("정상 응답을 받는다")
    public void receiveOkResponse() {
        response.then().statusCode(200);
    }
}
