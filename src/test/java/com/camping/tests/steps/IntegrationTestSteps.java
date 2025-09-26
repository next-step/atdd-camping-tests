package com.camping.tests.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

import static com.camping.tests.helper.Assertions.assertArrayLengthGreaterThanOrEqual;
import static com.camping.tests.helper.Assertions.hasGivenFieldsAsNotNullInArray;
import static com.camping.tests.helper.Context.*;
import static com.camping.tests.helper.LoginRequestFactory.getLoginRequest;
import static com.camping.tests.helper.RequestSender.getWithAuth;
import static com.camping.tests.helper.RequestSender.post;

/**
 * 서비스 간 통합 테스트를 위한 Step Definitions
 */
public class IntegrationTestSteps {

    @Given("관리자 시스템에서 토큰을 받았다")
    public void 관리자시스템에서토큰을받았다() {
        var request = getLoginRequest();
        lastResponse = post(adminBaseUrl, "/auth/login", request);
        authToken = lastResponse.jsonPath().getString("accessToken");
    }

    @When("키오스크 상품 목록을 조회한다")
    public void 키오스크상품목록을조회한다() {
        lastResponse = getWithAuth(kioskBaseUrl, "/api/products", authToken);
    }

    @And("응답 배열의 길이가 {int} 이상이다")
    public void 응답배열의길이가이상이다(int expectedMinLength) {
        assertArrayLengthGreaterThanOrEqual(lastResponse, expectedMinLength);
    }

    @And("상품 정보의 주요 필드들이 존재한다")
    public void 상품정보의주요필드들이존재한다() {
        hasGivenFieldsAsNotNullInArray(lastResponse, "id", "name", "price");
    }
}