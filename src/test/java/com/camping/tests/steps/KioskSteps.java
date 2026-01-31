package com.camping.tests.steps;

import com.camping.tests.api.KioskApi;
import com.camping.tests.context.HttpContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
public class KioskSteps {
    private final KioskApi kioskApi;
    private final HttpContext httpContext;

    public KioskSteps(KioskApi kioskApi, HttpContext httpContext) {
        this.kioskApi = kioskApi;
        this.httpContext = httpContext;
    }

    @When("키오스크 시스템의 상태를 확인하면")
    public void 키오스크에_시스템의_상태를_확인하면() {
        kioskApi.헬스_체크();
    }

    @When("키오스크의 상품 목록을 조회하면")
    public void 키오스크의_상품_목록을_조회하면() {
        kioskApi.상품_목록_조회_요청();
    }

    @Then("상품 목록은 1개 이상이다")
    public void 상품_목록은_1개_이상이다() {
        List<Object> list = httpContext.getResponse().jsonPath().getList(".");
        assertThat(list.isEmpty()).isFalse();
    }
}
