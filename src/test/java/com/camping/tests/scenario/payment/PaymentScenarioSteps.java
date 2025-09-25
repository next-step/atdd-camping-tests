package com.camping.tests.scenario.payment;

import com.camping.tests.scenario.TestContext;
import com.camping.tests.steps.kiosk.dto.KioskConfirmPaymentRequest;
import com.camping.tests.steps.kiosk.dto.KioskCreatePaymentRequest;
import com.camping.tests.steps.kiosk.dto.KioskCreatePaymentRequest.Item;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static com.camping.tests.steps.kiosk.KioskProductTestSteps.*;

public class PaymentScenarioSteps {
    @When("키오스크에서 {string} 상품의 결제를 생성한다")
    public void 키오스크에서_XX_상품_결제를_생성한다(String productName) {
        var 결제할_상품 = 상품_목록에서_상품이_조회된다(productName);
        TestContext.Payment.결제할_상품(결제할_상품);

        var 결제_생성_응답 = 결제를_생성한다(
            KioskCreatePaymentRequest.fixture()
                .items(List.of(
                    Item.builder()
                        .productId(결제할_상품.id())
                        .productName(결제할_상품.name())
                        .unitPrice(결제할_상품.price())
                        .quantity(2)
                        .build()
                ))
                .create()
        );
        TestContext.Payment.결제_생성_응답(결제_생성_응답);
    }

    @Then("키오스크에서 결제 생성이 성공한다")
    public void 키오스크에서_결제_생성이_성공한다() {
        var 결제_생성_응답 = TestContext.Payment.결제_생성_응답();
        결제_생성이_성공한다(결제_생성_응답);
    }

    @When("키오스크에서 결제를 승인한다")
    public void 키오스크에서_결제를_승인한다() {
        var 결제_생성_응답 = TestContext.Payment.결제_생성_응답();
        var 결제_생성_결과 = 결제_생성_결과를_가져온다(결제_생성_응답);

        var 결제할_상품 = TestContext.Payment.결제할_상품();
        var 상품_결제_승인_응답 = 결제를_승인한다(
            KioskConfirmPaymentRequest.fixture()
                .paymentKey(결제_생성_결과.paymentKey())
                .orderId(결제_생성_결과.orderId())
                .amount(결제_생성_결과.amount())
                .items(
                    List.of(
                        KioskConfirmPaymentRequest.Item.builder()
                            .productId(결제할_상품.id())
                            .productName(결제할_상품.name())
                            .unitPrice(결제할_상품.price())
                            .quantity(2)
                            .build()
                    )
                )
                .create()
        );
        TestContext.Payment.상품_결제_승인_응답(상품_결제_승인_응답);
    }

    @Then("키오스크에서 결제 승인이 성공한다")
    public void 키오스크에서_결제_승인이_성공한다() {
        var 상품_결제_승인_응답 = TestContext.Payment.상품_결제_승인_응답();
        결제_승인이_성공한다(상품_결제_승인_응답);
    }

    @When("키오스크에서 잘못된 결제 키로 결제를 승인한다")
    public void 키오스크에서_잘못된_결제_키로_결제를_승인한다() {
        var 결제_생성_응답 = TestContext.Payment.결제_생성_응답();
        var 결제_생성_결과 = 결제_생성_결과를_가져온다(결제_생성_응답);

        var 결제할_상품 = TestContext.Payment.결제할_상품();
        var 상품_결제_승인_응답 = 결제를_승인한다(
            KioskConfirmPaymentRequest.fixture()
                .wrongPaymentKey()
                .orderId(결제_생성_결과.orderId())
                .amount(결제_생성_결과.amount())
                .items(
                    List.of(
                        KioskConfirmPaymentRequest.Item.builder()
                            .productId(결제할_상품.id())
                            .productName(결제할_상품.name())
                            .unitPrice(결제할_상품.price())
                            .quantity(2)
                            .build()
                    )
                )
                .create()
        );
        TestContext.Payment.상품_결제_승인_응답(상품_결제_승인_응답);
    }

    @Then("키오스크에서 결제 승인이 실패한다")
    public void 키오스크에서_결제_승인이_실패한다() {
        var 상품_결제_승인_응답 = TestContext.Payment.상품_결제_승인_응답();
        결제_승인이_실패한다(상품_결제_승인_응답);
    }
}
