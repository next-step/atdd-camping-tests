package com.camping.tests.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.util.List;

import static com.camping.tests.config.ExternalAPIConfig.getServiceHost;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
public class KioskProductSteps {
    private ExtractableResponse<Response> response;

    @When("키오스크의 상품 목록 조회 API를 호출하면")
    public void 키오스크의_상품_목록_조회_API를_호출하면() {
        response = RestAssured.given().log().all()
                .when()
                .get(getServiceHost("키오스크시스템") + "/api/products")
                .then().log().all()
                .extract();
    }

    @Then("응답된 상품 목록이 1개 이상이어야 한다")
    public void 응답된_상품_목록이_N개_이상이어야_한다() {
        List<Object> list = response.jsonPath().getList(".");
        assertThat(list.isEmpty()).isFalse();
    }
}
