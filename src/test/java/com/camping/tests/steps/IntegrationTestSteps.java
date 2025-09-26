package com.camping.tests.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.When;

import java.util.HashMap;
import java.util.Map;

import static com.camping.tests.helper.Assertions.*;
import static com.camping.tests.helper.Context.*;
import static com.camping.tests.helper.RequestSender.*;

/**
 * 서비스 간 통합 테스트를 위한 Step Definitions
 */
public class IntegrationTestSteps {

    @When("Admin 로그인 API {string}에 관리자 계정으로 로그인 요청을 보낸다")
    public void admin로그인API에관리자계정으로로그인요청을보낸다(String endpoint) {
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "admin123");

        lastResponse = post(adminBaseUrl, endpoint, loginRequest);
    }

    @And("인증 토큰 또는 쿠키를 받는다")
    public void 인증토큰또는쿠키를받는다() {
        assertHasAuthToken(lastResponse);

        // 응답에서 토큰 추출
        String responseBody = lastResponse.getBody().asString();
        if (responseBody.contains("token")) {
            authToken = lastResponse.jsonPath().getString("token");
        } else if (responseBody.contains("accessToken")) {
            authToken = lastResponse.jsonPath().getString("accessToken");
        }

        // 쿠키가 있다면 저장
        if (!lastResponse.getCookies().isEmpty()) {
            authCookies = lastResponse.getCookies();
        }
    }

    @When("Kiosk 상품 목록 API {string}에 요청을 보낸다")
    public void kiosk상품목록API에요청을보낸다(String endpoint) {
        // 인증 정보가 있다면 함께 전송
        if (authToken != null) {
            lastResponse = getWithAuth(kioskBaseUrl, endpoint, authToken);
        } else if (authCookies != null) {
            lastResponse = getWithCookies(kioskBaseUrl, endpoint, authCookies);
        } else {
            // 인증 정보가 없어도 일단 요청 (Kiosk는 public API일 수 있음)
            lastResponse = get(kioskBaseUrl, endpoint);
        }
    }

    @And("응답 배열의 길이가 {int} 이상이다")
    public void 응답배열의길이가이상이다(int expectedMinLength) {
        assertArrayLengthGreaterThanOrEqual(lastResponse, expectedMinLength);
    }

    @And("상품 정보의 주요 필드들이 존재한다")
    public void 상품정보의주요필드들이존재한다() {
        assertProductListResponse(lastResponse);
    }
}
