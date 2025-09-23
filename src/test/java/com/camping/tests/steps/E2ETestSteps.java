package com.camping.tests.steps;


import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.cucumber.datatable.DataTable;
import io.restassured.response.Response;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;


public class E2ETestSteps {
    private String authToken;
    private Response response;
    private final String kioskBaseUrl;
    private final String adminBaseUrl;


    public E2ETestSteps() {
        this.kioskBaseUrl = System.getProperty("KIOSK_BASE_URL", "http://localhost:8081");
        this.adminBaseUrl = System.getProperty("ADMIN_BASE_URL", "http://localhost:8082");
    }

        @Given("어드민 서비스에 로그인 요청을 한다")
    public void adminLogin(DataTable dataTable) {
        Map<String, String> credentials = dataTable.asMaps().get(0);

        response = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "username", credentials.get("username"),
                        "password", credentials.get("password")
                ))
                .when()
                .post(adminBaseUrl + "/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        authToken = response.getCookie("AUTH_TOKEN");
    }

    @When("키오스크 서비스의 상품 목록을 조회한다")
    public void getKioskProducts() {
        response = given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(kioskBaseUrl + "/api/products");
    }

    @Then("상품 목록이 정상적으로 조회된다")
    public void verifyProductListResponse() {
        response.then()
                .statusCode(200);
    }

    @And("최소 1개 이상의 상품이 존재한다")
    public void verifyProductListNotEmpty() {
        List<Map<String, Object>> products = response.jsonPath().getList("$");
        assertThat(products).isNotEmpty();
    }

    @And("각 상품은 필수 필드를 포함한다")
    public void verifyProductFields() {
        List<Map<String, Object>> products = response.jsonPath().getList("$");
        products.forEach(product -> {
            assertThat(product).containsKeys("id", "name", "price");
        });
    }
}
