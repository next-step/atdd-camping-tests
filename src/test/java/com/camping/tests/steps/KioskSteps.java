package com.camping.tests.steps;

import com.camping.tests.helper.*;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static com.camping.tests.helper.KioskAssertions.*;
import static com.camping.tests.helper.KioskContext.lastResponse;
import static com.camping.tests.helper.KioskRequestSender.*;

/**
 * 키오스크 애플리케이션 인수테스트를 위한 Step Definitions
 */
public class KioskSteps {

    @Before
    public void setUp() {
        if (KioskContext.kioskBaseUrl == null) {
            KioskConfigurationHelper.initializeRestAssured();
        }
        KioskContext.reset();
    }

    @Given("키오스크 애플리케이션이 준비되어 있다")
    public void 키오스크_애플리케이션이_준비되어_있다() {
        KioskApplicationWaiter.waitForApplicationReady();
    }

    @When("키오스크 홈페이지 {string}에 요청을 보낸다")
    public void 키오스크_홈페이지에_요청을_보낸다(String endpoint) {
        lastResponse = getWithTiming(endpoint);
    }

    @When("키오스크 CSS 파일 {string}에 요청을 보낸다")
    public void 키오스크_CSS_파일에_요청을_보낸다(String endpoint) {
        lastResponse = get(endpoint);
    }

    @When("키오스크 헬스체크 {string}에 요청을 보낸다")
    public void 키오스크_헬스체크에_요청을_보낸다(String endpoint) {
        lastResponse = get(endpoint);
    }

    @When("키오스크 상품 API {string}에 요청을 보낸다")
    public void 키오스크_상품_API에_요청을_보낸다(String endpoint) {
        lastResponse = get(endpoint);
    }

    @When("키오스크 {string}에 요청을 보낸다")
    public void 키오스크에_요청을_보낸다(String endpoint) {
        lastResponse = get(endpoint);
    }

    @Then("200 응답을 받는다")
    public void 응답을_받는다() {
        assertSuccessResponse(lastResponse);
    }

    @Then("HTML 콘텐츠를 받는다")
    public void HTML_콘텐츠를_받는다() {
        assertHtmlContent(lastResponse);
    }

    @Then("{int}초 이내에 응답을 받는다")
    public void 초_이내에_응답을_받는다(int expectedSeconds) {
        assertResponseTime(expectedSeconds);
    }

    @Then("정적 리소스 응답을 받는다")
    public void 정적_리소스_응답을_받는다() {
        assertStaticResourceResponse(lastResponse);
    }

    @Then("헬스체크 응답을 받는다")
    public void 헬스체크_응답을_받는다() {
        assertHealthCheckResponse(lastResponse);
    }

    @Then("API 응답을 받는다")
    public void API_응답을_받는다() {
        assertApiResponse(lastResponse);
    }

    @Then("정상적인 응답을 받는다")
    public void 정상적인_응답을_받는다() {
        assertNormalResponse(lastResponse);
    }
}
