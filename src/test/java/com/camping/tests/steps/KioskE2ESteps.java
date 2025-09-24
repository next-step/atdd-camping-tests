package com.camping.tests.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
public class KioskE2ESteps {

    private String authToken;
    private Response lastResponse;

    private static final String ADMIN_BASE_URL = "http://localhost:18082";
    private static final String KIOSK_BASE_URL = "http://localhost:18081";

    @Given("어드민 서비스로 로그인 API를 호출해 토큰을 발급 받아")
    public void 어드민_서비스로_로그인_API를_호출해_토큰을_발급_받아() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"admin\",\"password\":\"admin123\"}")
        .when()
                .post(ADMIN_BASE_URL + "/auth/login")
        .then()
                .statusCode(200)
                .extract().response();

        String cookieToken = response.getCookie("AUTH_TOKEN");
        String jsonToken = response.jsonPath().getString("accessToken");
        this.authToken = cookieToken != null ? cookieToken : jsonToken;

        assertThat(cookieToken).isNotNull();
    }

    @When("키오스크 서비스의 상품 목록 조회를 호출하면")
    public void 키오스크_서비스의_상품_목록_조회를_호출하면() {
        this.lastResponse = given()
                .cookie("AUTH_TOKEN", this.authToken)
        .when()
                .get(KIOSK_BASE_URL + "/api/products")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response();
    }

    @Then("상품이 {int}개 이상 조회된다")
    public void 상품이_조회된다(int expectedMinCount) {
        int actualCount = this.lastResponse.jsonPath().getList("$").size();

        assertThat(actualCount).isGreaterThanOrEqualTo(expectedMinCount);
    }
}
