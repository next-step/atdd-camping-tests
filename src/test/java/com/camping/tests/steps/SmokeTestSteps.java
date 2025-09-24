
package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;


import static io.restassured.RestAssured.given;

public class SmokeTestSteps {

    private Response response;
    private final String kioskBaseUrl;
    private final String adminBaseUrl;
    private final String reservationBaseUrl;

    public SmokeTestSteps() {
        this.kioskBaseUrl = System.getProperty("KIOSK_BASE_URL");
        this.adminBaseUrl = System.getProperty("ADMIN_BASE_URL");
        this.reservationBaseUrl = System.getProperty("RESERVATION_BASE_URL");
    }

    @When("키오스크 서비스의 상태 확인 엔드포인트로 요청을 보내면")
    public void requestKioskHealthCheck() {
        response = given()
                .when()
                .get(kioskBaseUrl);
    }

    @When("어드민 서비스의 상태 확인 엔드포인트로 요청을 보내면")
    public void requestAdminHealthCheck() {
        response = given()
                .when()
                .get(adminBaseUrl + "/login");
    }

    @When("예약 서비스의 상태 확인 엔드포인트로 요청을 보내면")
    public void requestReservationHealthCheck() {
        response = given()
                .when()
                .get(reservationBaseUrl);
    }

    @Then("정상 응답을 받는다")
    public void receiveOkResponse() {
        response.then().statusCode(200);
    }
}
