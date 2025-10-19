package com.camping.admin.steps;

import com.camping.common.support.ApiHelper;
import com.camping.common.support.CommonContext;
import io.cucumber.core.options.CurlOption.HttpMethod;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class AdminSteps {

    public AdminSteps() {
        RestAssured.baseURI = CommonContext.ADMIN_BASE_URL;
    }

    @When("admin 컨테이너에 요청을 보낸다")
    public void admin컨테이너에요청을보낸다() {
        CommonContext.lastResponse = ApiHelper.adminRequest(HttpMethod.GET, "/", null)
                .then().log().all()
                .extract().response();
    }

    @When("상품 목록 조회를 요청한다")
    public void 상품목록조회를요청한다() {
        CommonContext.lastResponse = ApiHelper.adminRequest(HttpMethod.GET, "admin/products", null)
                .then().log().all()
                .extract().response();
    }

    @Then("현재 상품 정보들을 받는다")
    public void 현재상품정보들을받는다() {
        CommonContext.lastResponse.then()
                .body("size()", greaterThanOrEqualTo(1));
    }
}
