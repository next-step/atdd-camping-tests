package com.camping.tests.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import static com.camping.tests.support.fixture.KioskTestFixture.*;

public class IntegrationSteps {

    private ExtractableResponse<Response> response;

    @When("회원은 키오스크에서 상품 목록을 조회한다.")
    public void 회원은키오스크에서상품목록을조회한다() {
        response = 키오스크_상품_목록_조회();
    }

    @Then("상품 목록이 {int}개 이상 나온다.")
    public void 상품목록이개이상나온다(int quantity) {
        상품_목록_개수_검증(response, quantity);
    }

    @And("상품에는 이름, 가격, 수량, 타입이 있다.")
    public void 상품에는이름가격수량타입이있다() {
        상품_기본_필드_검증_legacy(response);
    }
}
