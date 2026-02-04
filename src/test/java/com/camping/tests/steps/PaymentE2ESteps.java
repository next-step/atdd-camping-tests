package com.camping.tests.steps;

import com.camping.tests.dto.*;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.먼저;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.List;

import static io.restassured.RestAssured.given;
import static com.camping.tests.config.TestConfig.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class PaymentE2ESteps {

    private final SharedContext context;
    private List<CartItemDto> cartItems;

    public PaymentE2ESteps(SharedContext context) {
        this.context = context;
    }

    // --- 장바구니 설정 ---
    @먼저("키오스크에 상품 {string}, {int}, {int}가 담겨있다")
    public void 키오스크에_상품이_담겨있다(String name, int price, int quantity) {
        cartItems = List.of(new CartItemDto(1L, name, price, quantity));
    }

    // --- 결제 생성 ---
    @만약("키오스크에 결제 생성을 요청한다")
    public void 키오스크에_결제_생성을_요청한다() {
        PaymentCreateRequestDto request = new PaymentCreateRequestDto(cartItems, "CARD");

        Response response = given()
                .baseUri(KIOSK_BASE_URL)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/payments");

        context.setResponse(response);
    }

    // --- 결제 확정 (성공 케이스) ---
    @그리고("키오스크에 결제 확정을 요청한다")
    public void 키오스크에_결제_확정을_요청한다() {
        PaymentCreateResultDto createResult = context.getResponse().as(PaymentCreateResultDto.class);

        PaymentConfirmRequestDto request = new PaymentConfirmRequestDto(
                createResult.paymentKey(),
                createResult.orderId(),
                createResult.amount(),
                cartItems
        );

        Response response = given()
                .baseUri(KIOSK_BASE_URL)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/payments/confirm");

        context.setResponse(response);
    }

    // --- 결제 확정 (실패 케이스 - 금액 불일치) ---
    @그리고("키오스크에 금액 {int}원으로 결제 확정을 요청한다")
    public void 키오스크에_금액_원으로_결제_확정을_요청한다(int wrongAmount) {
        PaymentCreateResultDto createResult = context.getResponse().as(PaymentCreateResultDto.class);

        PaymentConfirmRequestDto request = new PaymentConfirmRequestDto(
                createResult.paymentKey(),
                createResult.orderId(),
                wrongAmount,
                cartItems
        );

        Response response = given()
                .baseUri(KIOSK_BASE_URL)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/payments/confirm");

        context.setResponse(response);
    }


    // --- 결제 결과 검증 ---
    @그러면("결제가 성공이어야 한다")
    public void 결제가_성공이어야_한다() {
        Response res = context.getResponse();
        assertThat(res.getStatusCode()).as("결제 확정 API 상태 코드 ").isEqualTo(200);
        PaymentConfirmResponseDto result = res.as(PaymentConfirmResponseDto.class);
        assertThat(result.success()).isTrue();
    }

    @그러면("결제가 실패이어야 한다")
    public void 결제가_실패이어야_한다() {
        PaymentConfirmResponseDto result = context.getResponse().as(PaymentConfirmResponseDto.class);
        assertThat(result.success()).isFalse();
    }
}
