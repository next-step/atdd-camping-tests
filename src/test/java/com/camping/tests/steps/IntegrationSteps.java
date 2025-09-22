package com.camping.tests.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class IntegrationSteps {
    private ExtractableResponse<Response> response;

    @When("회원은 키오스크에서 상품 목록을 조회한다.")
    public void 회원은키오스크에서상품목록을조회한다() {
        response = RestAssured.given()
                .log().all()
                .when()
                .get("http://localhost:18081/api/products")
                .then()
                .log().all()
                .extract();

         assertThat(response.statusCode()).isEqualTo(200);
    }

    @Then("상품 목록이 {int}개 이상 나온다.")
    public void 상품목록이개이상나온다(int quantity) {
        var products = response.jsonPath().getList("$");
        assertThat(products.size()).isGreaterThanOrEqualTo(quantity);
    }

    @And("상품에는 이름, 가격, 수량, 타입이 있다.")
    public void 상품에는이름가격수량타입이있다() {
        List<Map<String, Object>> products = response.jsonPath().getList("$");

        for (Map<String, Object> productMap : products) {
            assertThat(productMap).containsKey("name");
            assertThat(productMap).containsKey("price");
            assertThat(productMap).containsKey("stockQuantity");
            assertThat(productMap).containsKey("productType");

            assertThat(productMap.get("name")).isNotNull();
            assertThat(productMap.get("price")).isNotNull();
            assertThat(productMap.get("stockQuantity")).isNotNull();
            assertThat(productMap.get("productType")).isNotNull();
        }
    }

}
