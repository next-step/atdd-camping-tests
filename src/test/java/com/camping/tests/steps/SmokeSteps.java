package com.camping.tests.steps;

import com.camping.tests.CommonContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.ko.만약;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.util.Map;

public class SmokeSteps {
    @When("{string}에 요청을 보낸다")
    public void 요청을보낸다(String url) {
        CommonContext.lastResponse = RestAssured.given()
                .when().get(url)
                .then().log().all()
                .extract().response();
        System.out.println(url + "에 요청을 보냈다");
    }

    @만약("{string}경로에서 로그인을 한다.")
    public void 경로에서로그인을한다(String url) {
        CommonContext.lastResponse = RestAssured.given()
            .when().log().all()
            .contentType(ContentType.JSON)
            .body(Map.of("username", "admin", "password", "admin123"))
            .post(url)
            .then().log().all()
            .extract().response();
    }

    @Then("성공 응답을 받는다")
    public void 성공응답을받는다() {
        CommonContext.lastResponse.then().statusCode(200);
        System.out.println("# 성공 응답을 받는다");
    }
}


