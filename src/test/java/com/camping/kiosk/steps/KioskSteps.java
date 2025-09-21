package com.camping.kiosk.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class KioskSteps {

    public static final String KIOSK_BASE_URL = "KIOSK_BASE_URL";
    public static final String KIOSK_DEFAULT_URL = "http://localhost:18080";

    private Response response;

    public KioskSteps() {
        RestAssured.baseURI = System.getProperty(KIOSK_BASE_URL,
                System.getenv().getOrDefault(KIOSK_BASE_URL, KIOSK_DEFAULT_URL));
        System.out.println("KIOSK_BASE_URL: " + RestAssured.baseURI);
    }

    @When("키오스크 컨테이너에 요청을 보낸다")
    public void 키오스크컨테이너에요청을보낸다() {
        response = RestAssured.given().log().all()
                .when().get("/")
                .then().log().all()
                .extract().response();
    }

    @Then("성공 응답을 받는다")
    public void 성공응답을받는다() {
        response.then().statusCode(200);
    }
}


