package com.camping.tests.steps;

import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.그러면;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HealthCheckSteps {

    private Response response;

    @만약("{string}에 요청을 보낸다")
    public void 요청을보낸다(String url) {
        response = given()
                .when()
                .get(url);
    }

    @그러면("성공 응답을 받는다")
    public void 성공응답을받는다() {
        assertEquals(200, response.getStatusCode());
    }
}