package com.camping.tests.steps;

import com.camping.tests.context.HttpContext;
import io.cucumber.java.en.Then;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
public class ApiCommonSteps {
    private final HttpContext httpContext;

    public ApiCommonSteps(HttpContext httpContext) {
        this.httpContext = httpContext;
    }

    @Then("성공 응답을 받는다")
    public void 성공_응답을_받는다() {
        assertThat(httpContext.getResponse().statusCode()).isEqualTo(200);
    }
}
