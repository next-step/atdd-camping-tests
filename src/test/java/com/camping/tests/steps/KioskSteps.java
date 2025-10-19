package com.camping.tests.steps;

import com.camping.tests.context.CommonContextHolder;
import com.camping.tests.helpers.KioskApiHelper;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class KioskSteps {

    private CommonContextHolder context() {
        return CommonContextHolder.getInstance();
    }

    @Then("상품 목록이 정상적으로 조회된다")
    public void verifyProductListSuccess() {
        Response response = context().getResponse();
        response.then().statusCode(200);
    }

    @Then("최소 {int}개 이상의 상품이 존재한다")
    public void verifyMinimumProducts(Integer minCount) {
        Response response = context().getResponse();
        List<?> products = response.jsonPath().getList("$");
        assertThat(products.size()).isGreaterThanOrEqualTo(minCount);
    }
}
