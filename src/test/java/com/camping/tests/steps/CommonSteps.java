package com.camping.tests.steps;

import io.cucumber.java.ko.그러면;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    private final SharedContext context;

    public CommonSteps(SharedContext context) {
        this.context = context;
    }

    @그러면("응답 상태코드는 {int}이어야 한다")
    public void 응답_상태코드는_이어야_한다(int expectedStatusCode) {
        assertThat(context.getResponse().getStatusCode())
                .as("응답 상태코드")
                .isEqualTo(expectedStatusCode);
    }
}
