package com.camping.tests.steps;

import com.camping.tests.context.ScenarioContext;
import io.cucumber.java.ko.그러면;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    private final ScenarioContext context;

    public CommonSteps(ScenarioContext context) {
        this.context = context;
    }

    @그러면("성공 응답을 받는다")
    public void 성공_응답을_받는다() {
        var response = context.getResponse();
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @그러면("응답 본문은 {string}이다")
    public void 응답_본문을_확인한다(String body) {
        var response = context.getResponse();
        assertThat(response.body().asString()).isEqualTo(body);
    }
}
