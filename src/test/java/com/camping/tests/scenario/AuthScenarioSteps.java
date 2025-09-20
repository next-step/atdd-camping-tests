package com.camping.tests.scenario;

import static com.camping.tests.steps.admin.AdminAuthTestSteps.어드민으로_로그인이_되어있다;

import io.cucumber.java.en.Given;

public class AuthScenarioSteps {

    @Given("관리자 계정으로 로그인이 되어있다")
    public void 관리자_계정으로_로그인이_되어있다() {
        어드민으로_로그인이_되어있다();
    }
}
