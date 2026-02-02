package com.camping.tests.steps;

import com.camping.tests.clients.ApiClient;
import com.camping.tests.context.ScenarioContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

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
        var response = ApiClient.get(kioskBaseUrl + path);
        context.setResponse(response);
    }

    @When("키오스크로 상품 목록을 조회하면")
    public void 키오스크로_상품_목록을_조회하면() {
        var response = ApiClient.get(kioskBaseUrl + "/api/products");
        context.setResponse(response);
    }

    @Then("상품 개수는 1개 이상이다")
    public void 상품_개수는_1개_이상이다() {
        List<Object> list = context.getResponse().jsonPath().getList(".");
        assertThat(list).isNotEmpty();
    }
}


