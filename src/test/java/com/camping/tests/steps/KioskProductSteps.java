package com.camping.tests.steps;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;

public class KioskProductSteps {

    private Response response;


    @When("키오스크 앱으로 상품 조회 요청을 보낸다")
    public void 키오스크앱으로상품조회요청을보낸다() {
        response = given()
                .when().get("http://localhost:8080/api/products")
                .then().log().all()
                .extract().response();
    }

    @Then("상품 목록이 잘 조회된다")
    public void 상품목록이잘조회된다() {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(response.jsonPath().getList("$").size()).isGreaterThan(0);
    }
}
