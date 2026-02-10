package com.camping.tests.steps;

import com.camping.tests.api.PaymentApi;
import com.camping.tests.factory.PaymentRequestFactory;
import com.camping.tests.steps.context.TestContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaymentSteps {

    private static final String KIOSK_BASE_URL =
            System.getenv("KIOSK_BASE_URL") != null
                    ? System.getenv("KIOSK_BASE_URL")
                    : "http://localhost:18081";

    private static final int DEFAULT_CONFIRM_AMOUNT = 10000;
    private static final String DEFAULT_PAYMENT_METHOD = "CARD";

    @Autowired
    private TestContext testContext;

    @Autowired
    private PaymentApi paymentApi;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @만약("키오스크에 결제 생성을 요청한다")
    public void 키오스크에_결제_생성을_요청한다() {
        ensureDefaultCartItem();

        ExtractableResponse<Response> createResponse = paymentApi.결제_생성(
                KIOSK_BASE_URL,
                testContext.getPayment().getCartItemFixture(),
                DEFAULT_PAYMENT_METHOD
        );
        validatePaymentCreateResponse(createResponse);
        testContext.setResponse(createResponse);
    }

    @그리고("키오스크에 결제 확정을 요청한다")
    public void 키오스크에_결제_확정을_요청한다() {
        testContext.setResponse(paymentApi.결제_확정(
                KIOSK_BASE_URL,
                testContext.getPayment().getPaymentKey(),
                testContext.getPayment().getOrderId(),
                DEFAULT_CONFIRM_AMOUNT,
                testContext.getPayment().getCartItemFixture()
        ));
    }

    @그리고("키오스크에 금액 {int}원으로 결제 확정을 요청한다")
    public void 키오스크에_금액_N원으로_결제_확정을_요청한다(int amount) {
        testContext.setResponse(paymentApi.결제_확정(
                KIOSK_BASE_URL,
                testContext.getPayment().getPaymentKey(),
                testContext.getPayment().getOrderId(),
                amount,
                testContext.getPayment().getCartItemFixture()
        ));
    }

    @그리고("결제 요청 body 아이템을 다음 값으로 설정한다")
    public void 결제_요청_body_아이템을_다음_값으로_설정한다(String cartItemBody) {
        try {
            testContext.getPayment().setCartItemFixture(
                    objectMapper.readValue(cartItemBody, new TypeReference<Map<String, Object>>() {})
            );
        } catch (Exception e) {
            throw new AssertionError("결제 요청 body 아이템 JSON 파싱에 실패했습니다", e);
        }
    }

    @그러면("결제가 성공이어야 한다")
    public void 결제가_성공이어야_한다() {
        assertEquals(200, testContext.getResponse().statusCode());
        assertTrue(testContext.getResponse().jsonPath().getBoolean("success"), "결제가 성공해야 합니다");
    }

    @그러면("결제가 실패이어야 한다")
    public void 결제가_실패이어야_한다() {
        boolean statusFailure = testContext.getResponse().statusCode() == 400;
        boolean bodyFailure = !Boolean.TRUE.equals(testContext.getResponse().jsonPath().getBoolean("success"));
        assertTrue(statusFailure || bodyFailure, "결제가 실패해야 합니다");
    }

    private void ensureDefaultCartItem() {
        if (testContext.getPayment().getCartItemFixture() == null) {
            testContext.getPayment().setCartItemFixture(PaymentRequestFactory.defaultCartItemFixture());
        }
    }

    private void validatePaymentCreateResponse(ExtractableResponse<Response> createResponse) {
        assertEquals(200, createResponse.statusCode());

        String paymentKey = createResponse.jsonPath().getString("paymentKey");
        String orderId = createResponse.jsonPath().getString("orderId");

        assertNotNull(paymentKey, "paymentKey는 null이면 안 됩니다");
        assertNotNull(orderId, "orderId는 null이면 안 됩니다");

        testContext.getPayment().setPaymentKey(paymentKey);
        testContext.getPayment().setOrderId(orderId);
    }
}
