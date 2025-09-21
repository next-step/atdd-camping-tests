package com.camping.admin.steps;

import com.camping.common.support.AdminApiHelper;
import com.camping.common.support.CommonContext;
import io.cucumber.core.options.CurlOption.HttpMethod;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;

public class AdminSteps {

    public AdminSteps() {
        RestAssured.baseURI = CommonContext.ADMIN_BASE_URL;
    }

    @When("admin 컨테이너에 요청을 보낸다")
    public void admin컨테이너에요청을보낸다() {
        CommonContext.lastResponse = AdminApiHelper.request(HttpMethod.GET, "/", null)
                .then().log().all()
                .extract().response();
    }
}
