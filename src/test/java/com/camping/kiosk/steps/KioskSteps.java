package com.camping.kiosk.steps;

import com.camping.common.support.CommonContext;
import com.camping.common.support.KioskApiHelper;
import io.cucumber.core.options.CurlOption;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;

public class KioskSteps {

    public KioskSteps() {
        RestAssured.baseURI = CommonContext.KIOSK_BASE_URL;
    }

    @When("키오스크 컨테이너에 요청을 보낸다")
    public void 키오스크컨테이너에요청을보낸다() {
        CommonContext.lastResponse = KioskApiHelper.request(CurlOption.HttpMethod.GET, "/", null)
                .then().log().all()
                .extract().response();
    }

}


