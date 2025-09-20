package com.camping.tests.scenario;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ProductScenarioSteps {
    @When("상품을 등록한다")
    public void 상품을_등록한다() {
        System.out.println("상품 등록");
    }

    @Then("상품 등록이 성공한다")
    public void 상품_등록이_성공한다() {
        System.out.println("상품 등록 성공");
    }

    @Then("등록한 상품이 조회된다")
    public void 등록한_상품이_조회된다() {
        System.out.println("상품 조회 성공");
    }
}
