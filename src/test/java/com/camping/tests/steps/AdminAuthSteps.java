package com.camping.tests.steps;

import com.camping.tests.api.AdminApi;
import com.camping.tests.context.AdminContext;
import com.camping.tests.context.HttpContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
public class AdminAuthSteps {
    private final AdminContext adminContext;
    private final HttpContext httpContext;
    private final AdminApi adminApi;

    public AdminAuthSteps(AdminContext adminContext, HttpContext httpContext, AdminApi adminApi) {
        this.adminContext = adminContext;
        this.httpContext = httpContext;
        this.adminApi = adminApi;
    }

    @Given("유효한 어드민 계정이 존재한다")
    public void 유효한_어드민_계정이_존재한다() {
        System.out.println("유효한 어드민 계정이 존재한다");
    }

    @Given("어드민은 인증된 상태이다")
    public void 어드민은_인증된_상태이다() {
        adminApi.로그인_요청();
    }

    @When("어드민에 로그인하면")
    public void 어드민에_로그인하면() {
        adminApi.로그인_요청();
    }

    @Then("로그인에 성공한다")
    public void 로그인에_성공한다() {
        assertThat(httpContext.getResponse().statusCode()).isEqualTo(200);
    }

    @Then("인증 토큰이 발급된다")
    public void 인증_토큰이_발급된다() {
        String token = adminContext.getAdminToken();
        assertThat(token).isNotBlank();
    }

    @When("어드민 시스템의 상태를 확인하면")
    public void 어드민_시스템의_상태를_확인하면() {
        String token = adminContext.getAdminToken();
        adminApi.헬스_체크(token);
    }

    @Given("어드민에 상품이 등록되어 있다")
    public void 어드민에_상품이_등록되어_있다() {
        System.out.println("어드민에 상품이 등록되어 있다");
    }
}
