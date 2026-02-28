package com.camping.tests.steps;

import com.camping.tests.support.TestConfig;
import com.camping.tests.support.TestContext;
import io.cucumber.java.ko.만약;
import io.restassured.RestAssured;

public class SmokeSteps {

    @만약("키오스크 헬스 체크를 요청한다")
    public void 키오스크_헬스_체크() {
        TestContext.current().setLastResponse(
                RestAssured.given().get(TestConfig.KIOSK_BASE_URL + "/health"));
    }
}
