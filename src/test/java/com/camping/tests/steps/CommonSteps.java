package com.camping.tests.steps;

import com.camping.tests.context.ScenarioContext;
import io.cucumber.java.en.Then;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    private final ScenarioContext context;

    public CommonSteps(ScenarioContext context) {
        this.context = context;
    }

    @Then("성공 응답을 받는다")
    public void 성공_응답을_받는다() {
        assertThat(context.getResponse().statusCode()).isEqualTo(200);
    }

    @Then("응답 본문은 {string}이다")
    public void 응답_본문을_확인한다(String body) {
        assertThat(context.getResponse().getBody().asString()).isEqualTo(body);
    }
}
