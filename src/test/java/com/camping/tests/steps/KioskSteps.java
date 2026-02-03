package com.camping.tests.steps;

import com.camping.tests.clients.ApiClient;
import com.camping.tests.context.ScenarioContext;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class KioskSteps {

    private static final String orderId = "dummy-order-id";
    private static final String paymentKey = "dummy-payment-key";

    private final String kioskBaseUrl;
    private final ScenarioContext context;


    public KioskSteps(ScenarioContext context) {
        this.context = context;
        this.kioskBaseUrl = Optional.ofNullable(System.getenv("KIOSK_BASE_URL"))
                .orElse("http://localhost:8081");
    }

    @Before
    public void setupWireMock() {
        WireMock.configureFor("localhost", 8084);
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
        var response = ApiClient.get(kioskBaseUrl + "/api/products");
        context.setResponse(response);
    }

    @Given("키오스크에 결제 생성을 요청한다")
    public void 키오스크에_결제_생성을_요청한다() throws IOException {
        stubFor(post(urlEqualTo("/v1/payments"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"paymentKey\":\"dummy-payment-key\",\"orderId\":\"dummy-order-id\",\"status\":\"READY\"}")));

        // 2. Now, make the actual request to the Kiosk service.
        String kioskRequestBody = "{\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"productId\": 1,\n" +
                "      \"productName\": \"캠핑의자\",\n" +
                "      \"unitPrice\": 10000,\n" +
                "      \"quantity\": 1\n" +
                "    }\n" +
                "  ],\n" +
                "  \"paymentMethod\": \"CARD\"\n" +
                "}";

        var response = ApiClient.post(kioskBaseUrl + "/api/payments", kioskRequestBody);
        context.setResponse(response);
    }

    @When("키오스크에 결제 확정을 요청한다")
    public void 키오스크에_결제_확정을_요청한다() throws IOException {
        stubFor(post(urlEqualTo("/v1/payments/confirm"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"paymentKey\": \"" + paymentKey + "\",\n" +
                                "  \"orderId\": \"" + orderId + "\",\n" +
                                "  \"method\": \"CARD\",\n" +
                                "  \"approvedAt\": \"2026-02-02T10:00:00Z\",\n" +
                                "  \"totalAmount\": 10000,\n" +
                                "  \"status\": \"APPROVED\",\n" +
                                "  \"receipt\": {\n" +
                                "    \"url\": \"http://receipt.url/123\"\n" +
                                "  }\n" +
                                "}")));

        String requestBody = "{\n" +
                "  \"paymentKey\": \"" + paymentKey + "\",\n" +
                "  \"orderId\": \"" + orderId + "\",\n" +
                "  \"amount\": 10000,\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"productId\": 1,\n" +
                "      \"productName\": \"캠핑의자\",\n" +
                "      \"unitPrice\": 10000,\n" +
                "      \"quantity\": 1\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        var response = ApiClient.post(kioskBaseUrl + "/api/payments/confirm", requestBody);
        context.setResponse(response);
    }

    @When("키오스크에 금액 {int}원으로 결제 확정을 요청한다")
    public void 키오스크에_금액_원으로_결제_확정을_요청한다(int amount) {
        stubFor(post(urlEqualTo("/v1/payments/confirm"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"paymentKey\": \"" + paymentKey + "\",\n" +
                                "  \"orderId\": \"" + orderId + "\",\n" +
                                "  \"method\": \"CARD\",\n" +
                                "  \"approvedAt\": null,\n" +
                                "  \"totalAmount\": 0,\n" +
                                "  \"status\": \"REJECTED\",\n" +
                                "  \"receipt\": null\n" +
                                "}")));

        String requestBody = "{\n" +
                "  \"paymentKey\": \"" + paymentKey + "\",\n" +
                "  \"orderId\": \"" + orderId + "\",\n" +
                "  \"amount\": " + amount + ",\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"productId\": 1,\n" +
                "      \"productName\": \"캠핑의자\",\n" +
                "      \"unitPrice\": 10000,\n" +
                "      \"quantity\": 1\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        var response = ApiClient.post(kioskBaseUrl + "/api/payments/confirm", requestBody);
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



