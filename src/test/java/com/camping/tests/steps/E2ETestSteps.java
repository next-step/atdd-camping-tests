package com.camping.tests.steps;


import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.restassured.response.Response;
import com.camping.tests.context.CommonContext;
import java.util.List;
import java.util.Map;


public class E2ETestSteps {
    private final String kioskBaseUrl;
    private final CommonContext context;

    public E2ETestSteps() {
        this.kioskBaseUrl = System.getProperty("KIOSK_BASE_URL", "http://localhost:8081");
        this.context = new CommonContext();
    }

    @When("키오스크 서비스의 상품 목록을 조회한다")
    public void getKioskProducts() {
        Response response = CommonContext.getRequestSpec()
                .header("Authorization", "Bearer " + CommonContext.getAdminToken())
                .when()
                .get(kioskBaseUrl + "/api/products");

        context.setResponse(response);
    }

    @Then("상품 목록이 정상적으로 조회된다")
    public void verifyProductListResponse() {
        context.getResponse().then()
                .statusCode(200);
    }

    @And("최소 1개 이상의 상품이 존재한다")
    public void verifyProductListNotEmpty() {
        List<Map<String, Object>> products = context.getResponse().jsonPath().getList("$");
        assertThat(products).isNotEmpty();
    }

    @And("각 상품은 필수 필드를 포함한다")
    public void verifyProductFields() {
        List<Map<String, Object>> products = context.getResponse().jsonPath().getList("$");
        products.forEach(product -> {
            assertThat(product).containsKeys("id", "name", "price");
        });
    }
}
