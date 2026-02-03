package com.camping.tests.steps;

import com.camping.tests.dto.ConfirmResponse;
import com.camping.tests.dto.CreateResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.ko.먼저;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class MockPaymentSteps {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String orderId = "dummy-order-id";
    private static final String paymentKey = "dummy-payment-key";

    @먼저("결제 서버가 정상적으로 응답 가능한 상태이다")
    public void 결제_서버가_정상적으로_응답_가능한_상태이다() throws JsonProcessingException {
        var createResponse = new CreateResponse(paymentKey, orderId, "CREATED");
        stubFor(post(urlEqualTo("/v1/payments"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(createResponse))));

        var confirmResponse = new ConfirmResponse(paymentKey, orderId, "CARD", "2026-02-02T10:00:00Z", 10000, "APPROVED", new ConfirmResponse.Receipt("http://receipt.url/123"));
        stubFor(post(urlEqualTo("/v1/payments/confirm"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(confirmResponse))));
    }

    @먼저("결제 서버가 {string} 에러를 반환하도록 설정한다")
    public void 결제_서버가_에러를_반환하도록_설정한다(String errorType) throws JsonProcessingException {
        if ("잔액부족".equals(errorType)) {
            var confirmResponse = new ConfirmResponse(paymentKey, orderId, "CARD", null, 0, "REJECTED", null);
            stubFor(post(urlEqualTo("/v1/payments/confirm"))
                    .willReturn(aResponse()
                            .withStatus(400)
                            .withHeader("Content-Type", "application/json")
                            .withBody(objectMapper.writeValueAsString(confirmResponse))));
        }
    }
}
