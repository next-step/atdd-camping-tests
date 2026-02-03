package com.camping.tests.steps;

import com.camping.tests.clients.ApiClient;
import com.camping.tests.clients.KioskClient;
import com.camping.tests.config.TestConfig;
import com.camping.tests.context.ScenarioContext;
import com.camping.tests.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class KioskSteps {

    private static final String orderId = "dummy-order-id";
    private static final String paymentKey = "dummy-payment-key";

    private final String kioskBaseUrl;
    private final ScenarioContext context;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public KioskSteps(ScenarioContext context) {
        this.context = context;
        this.kioskBaseUrl = TestConfig.getKioskBaseUrl();
    }

    @Before
    public void setupWireMock() {
        WireMock.configureFor(TestConfig.getPaymentMockHost(), TestConfig.getPaymentMockPort());
        WireMock.reset();
    }

    @After
    public void teardownWireMock() {
        WireMock.reset();
    }

    @When("키오스크 서비스의 {string}에 GET 요청을 보낸다")
    public void 키오스크_서비스에_GET요청을_보낸다(String requestUrl) {
        var response = ApiClient.get(kioskBaseUrl + requestUrl);
        context.setResponse(response);
    }

    @When("키오스크로 상품 목록을 조회하면")
    public void 키오스크로_상품_목록을_조회하면() {
        var response = KioskClient.getProducts(kioskBaseUrl);
        context.setResponse(response);
    }

    @Given("키오스크에 결제 생성을 요청한다")
    public void 키오스크에_결제_생성을_요청한다() throws IOException {
        var createResponse = new CreateResponse(paymentKey, orderId, "CREATED");
        stubFor(post(urlEqualTo("/v1/payments"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(createResponse))));

        var cartItem = new CartItem(1L, "캠핑의자", 10000, 1);
        var kioskRequestBody = new PaymentCreateRequest(List.of(cartItem), "CARD");

        var response = KioskClient.createPayment(kioskBaseUrl, kioskRequestBody);
        context.setResponse(response);
    }

    @When("키오스크에 결제 확정을 요청한다")
    public void 키오스크에_결제_확정을_요청한다() throws IOException {
        var confirmResponse = new ConfirmResponse(paymentKey, orderId, "CARD", "2026-02-02T10:00:00Z", 10000, "APPROVED", new ConfirmResponse.Receipt("http://receipt.url/123"));
        stubFor(post(urlEqualTo("/v1/payments/confirm"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(confirmResponse))));

        var cartItem = new CartItem(1L, "캠핑의자", 10000, 1);
        var requestBody = new PaymentConfirmRequest(paymentKey, orderId, 10000, List.of(cartItem));

        var response = KioskClient.confirmPayment(kioskBaseUrl, requestBody);
        context.setResponse(response);
    }

    @When("키오스크에 금액 {int}원으로 결제 확정을 요청한다")
    public void 키오스크에_금액_원으로_결제_확정을_요청한다(int amount) throws IOException {
        var confirmResponse = new ConfirmResponse(paymentKey, orderId, "CARD", null, 0, "REJECTED", null);
        stubFor(post(urlEqualTo("/v1/payments/confirm"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(confirmResponse))));

        var cartItem = new CartItem(1L, "캠핑의자", 10000, 1);
        var requestBody = new PaymentConfirmRequest(paymentKey, orderId, amount, List.of(cartItem));

        var response = KioskClient.confirmPayment(kioskBaseUrl, requestBody);
        context.setResponse(response);
    }


    @Then("결제가 성공이어야 한다")
    public void 결제가_성공이어야_한다() {
        var response = context.getResponse();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("success")).isTrue();
        assertThat(response.jsonPath().getString("message")).isEqualTo("결제 성공");
        assertThat(response.jsonPath().getString("transactionId")).isEqualTo(paymentKey);
        assertThat(response.jsonPath().getInt("paidAmount")).isEqualTo(10000);
    }

    @Then("결제가 실패이어야 한다")
    public void 결제가_실패이어야_한다() {
        var response = context.getResponse();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("success")).isFalse();
        assertThat(response.jsonPath().getString("transactionId")).isNull();
        assertThat(response.jsonPath().getString("message")).isEqualTo("결제 또는 확정 실패");
        assertThat(response.jsonPath().getInt("paidAmount")).isZero();
    }

    @Then("상품 개수는 1개 이상이다")
    public void 상품_개수는_1개_이상이다() {
        List<Object> list = context.getResponse().jsonPath().getList(".");
        assertThat(list).isNotEmpty();
    }
}



