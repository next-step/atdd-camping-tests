package com.camping.tests.steps;

import com.camping.tests.context.ScenarioContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class KioskSteps {

    private final String kioskBaseUrl;
    private final ScenarioContext context;

    public KioskSteps(ScenarioContext context) {
        this.context = context;
        this.kioskBaseUrl = Optional.ofNullable(System.getenv("KIOSK_BASE_URL"))
                .orElse("http://localhost:8081");
    }

    @When("키오스크 서비스의 {string}에 GET 요청을 보낸다")
    public void 키오스크_서비스의_GET_요청을_보낸다(String path) {
        Response response = RestAssured.given()
                .when()
                .get(kioskBaseUrl + path);
        context.setResponse(response);
    }

    @When("키오스크로 상품 목록을 조회하면")
    public void 키오스크로_상품_목록을_조회하면() {
        ExtractableResponse<Response> response = RestAssured.given()
                .log().all()
                .baseUri(kioskBaseUrl)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products")
                .then()
                .log().all()
                .extract();

        context.setResponse(response.response());
    }

    @Then("상품 개수는 1개 이상이다")
    public void 상품_개수는_1개_이상이다() {
        List<Object> list = context.getResponse().jsonPath().getList(".");
        assertThat(list).isNotEmpty();
    }
}


