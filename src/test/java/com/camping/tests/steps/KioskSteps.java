package com.camping.tests.steps;

import com.camping.tests.api.KioskApi;
import com.camping.tests.context.HttpContext;
import com.camping.tests.context.KioskContext;
import com.camping.tests.dto.CartItemDto;
import com.camping.tests.dto.PaymentConfirmRequestDto;
import com.camping.tests.dto.PaymentConfirmResponseDto;
import com.camping.tests.dto.PaymentCreateRequestDto;
import com.camping.tests.dto.PaymentCreateResultDto;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
public class KioskSteps {
    private final KioskApi kioskApi;
    private final HttpContext httpContext;
    private final KioskContext kioskContext;

    public KioskSteps(KioskApi kioskApi, HttpContext httpContext, KioskContext kioskContext) {
        this.kioskApi = kioskApi;
        this.httpContext = httpContext;
        this.kioskContext = kioskContext;
    }

    @When("키오스크 시스템의 상태를 확인하면")
    public void 키오스크에_시스템의_상태를_확인하면() {
        kioskApi.헬스_체크();
    }

    @When("키오스크의 상품 목록을 조회하면")
    public void 키오스크의_상품_목록을_조회하면() {
        kioskApi.상품_목록_조회_요청();
    }

    @Then("상품 목록은 1개 이상이다")
    public void 상품_목록은_1개_이상이다() {
        List<Object> list = httpContext.getResponse().jsonPath().getList(".");
        assertThat(list.isEmpty()).isFalse();
    }

    @Given("키오스크에 상품 {string}, {int}, {int}가 담겨있다")
    public void 키오스크에_상품이_담겨있다(String name, int price, int quantity) {
        kioskContext.setCartItems(List.of(new CartItemDto(1L, name, price, quantity)));
    }

    @When("키오스크에 결제 생성을 요청한다")
    public void 키오스크에_결제_생성을_요청한다() {
        List<CartItemDto> items = kioskContext.getCartItems();
        kioskApi.결제_생성_요청(new PaymentCreateRequestDto(items, "CARD"));
    }

    @When("키오스크에 결제 확정을 요청한다")
    public void 키오스크에_결제_확정을_요청한다() {
        PaymentCreateResultDto createResult = httpContext.getResponse().as(PaymentCreateResultDto.class);

        kioskApi.결제_확정_요청(new PaymentConfirmRequestDto(
                createResult.paymentKey(),
                createResult.orderId(),
                createResult.amount(),
                List.of()
        ));
    }

    @Then("결제가 성공이어야 한다")
    public void 결제가_성공이어야_한다() {
        PaymentConfirmResponseDto confirmResponse = httpContext.getResponse().as(PaymentConfirmResponseDto.class);

        assertThat(confirmResponse.success()).isTrue();
        assertThat(confirmResponse.message()).isEqualTo("결제 성공");
        assertThat(confirmResponse.paidAmount()).isGreaterThanOrEqualTo(0);
    }

    @When("키오스크에 금액 {int}원으로 결제 확정을 요청한다")
    public void 키오스크에_금액으로_결제_확정을_요청한다(int amount) {
        PaymentCreateResultDto createResult = httpContext.getResponse().as(PaymentCreateResultDto.class);

        kioskApi.결제_확정_요청(new PaymentConfirmRequestDto(
                createResult.paymentKey(),
                createResult.orderId(),
                amount,
                List.of()
        ));
    }

    @Then("결제가 실패이어야 한다")
    public void 결제가_실패이어야_한다() {
        PaymentConfirmResponseDto confirmResponse = httpContext.getResponse().as(PaymentConfirmResponseDto.class);
        assertThat(confirmResponse.success()).isFalse();
    }
}
