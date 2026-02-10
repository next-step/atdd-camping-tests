package com.camping.tests.steps;

import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentSteps {

    private static final String KIOSK_BASE_URL =
            System.getenv("KIOSK_BASE_URL") != null
                    ? System.getenv("KIOSK_BASE_URL")
                    : "http://localhost:18081";

    private static final String CART_ITEM = """
            {
                "productId": 1,
                "productName": "테스트상품",
                "unitPrice": 10000,
                "quantity": 1
            }
            """;

    private Response createResponse;
    private Response confirmResponse;
    private String paymentKey;
    private String orderId;

    @만약("키오스크에 결제 생성을 요청한다")
    public void 키오스크에_결제_생성을_요청한다() {
        String requestBody = """
                {
                    "items": [%s],
                    "paymentMethod": "CARD"
                }
                """.formatted(CART_ITEM);

        createResponse = RestAssured
                .given()
                    .baseUri(KIOSK_BASE_URL)
                    .contentType("application/json")
                    .body(requestBody)
                .when()
                    .post("/api/payments");

        assertEquals(200, createResponse.statusCode());
        paymentKey = createResponse.jsonPath().getString("paymentKey");
        orderId = createResponse.jsonPath().getString("orderId");
        assertNotNull(paymentKey);
    }

    @그리고("키오스크에 결제 확정을 요청한다")
    public void 키오스크에_결제_확정을_요청한다() {
        String requestBody = """
                {
                    "paymentKey": "%s",
                    "orderId": "%s",
                    "amount": 10000,
                    "items": [%s]
                }
                """.formatted(paymentKey, orderId, CART_ITEM);

        confirmResponse = RestAssured
                .given()
                    .baseUri(KIOSK_BASE_URL)
                    .contentType("application/json")
                    .body(requestBody)
                .when()
                    .post("/api/payments/confirm");
    }

    @그리고("키오스크에 금액 {int}원으로 결제 확정을 요청한다")
    public void 키오스크에_금액_N원으로_결제_확정을_요청한다(int amount) {
        String requestBody = """
                {
                    "paymentKey": "%s",
                    "orderId": "%s",
                    "amount": %d,
                    "items": [%s]
                }
                """.formatted(paymentKey, orderId, amount, CART_ITEM);

        confirmResponse = RestAssured
                .given()
                    .baseUri(KIOSK_BASE_URL)
                    .contentType("application/json")
                    .body(requestBody)
                .when()
                    .post("/api/payments/confirm");
    }

    @그러면("결제가 성공이어야 한다")
    public void 결제가_성공이어야_한다() {
        assertEquals(200, confirmResponse.statusCode());
        Boolean success = confirmResponse.jsonPath().getBoolean("success");
        assertTrue(success, "결제가 성공해야 합니다");
    }

    @그러면("결제가 실패이어야 한다")
    public void 결제가_실패이어야_한다() {
        // 실패 시 400 에러 또는 success=false
        assertTrue(
            confirmResponse.statusCode() == 400 ||
            !confirmResponse.jsonPath().getBoolean("success"),
            "결제가 실패해야 합니다"
        );
    }
}