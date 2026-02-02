package com.camping.tests.steps;

import com.camping.tests.clients.ApiClient;
import com.camping.tests.context.ScenarioContext;
import io.cucumber.java.en.When;

import java.util.Optional;

public class AdminSteps {
    private final String adminBaseUrl;
    private final ScenarioContext context;

    public AdminSteps(ScenarioContext context) {
        this.context = context;
        this.adminBaseUrl = Optional.ofNullable(System.getenv("ADMIN_BASE_URL"))
                .orElse("http://localhost:8082");
    }

    @When("관리자 서비스의 {string}에 GET 요청을 보낸다")
    public void 관리자_서비스의_GET_요청을_보낸다(String path) {
        var response = ApiClient.get(adminBaseUrl + path);
        context.setResponse(response);
    }
}
