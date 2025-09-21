package com.camping.tests.scenario.payment;

import static com.camping.tests.steps.kiosk.KioskProductTestSteps.결제_생성_결과를_가져온다;
import static com.camping.tests.steps.kiosk.KioskProductTestSteps.결제_생성이_성공한다;
import static com.camping.tests.steps.kiosk.KioskProductTestSteps.결제_승인이_성공한다;
import static com.camping.tests.steps.kiosk.KioskProductTestSteps.결제를_생성한다;
import static com.camping.tests.steps.kiosk.KioskProductTestSteps.결제를_승인한다;
import static com.camping.tests.steps.kiosk.KioskProductTestSteps.상품_목록에서_상품이_조회된다;

import com.camping.tests.steps.kiosk.dto.KioskConfirmPaymentRequest;
import com.camping.tests.steps.kiosk.dto.KioskCreatePaymentRequest;
import com.camping.tests.steps.kiosk.dto.KioskCreatePaymentRequest.Item;
import com.camping.tests.steps.kiosk.dto.KioskProductDetail;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.List;

public class PaymentScenarioSteps {
    private KioskProductDetail 결제할_상품;
    private Response 결제_생성_응답;
    private Response 상품_결제_승인_응답;

    @When("키오스크에서 {string} 상품의 결제를 생성한다")
    public void 키오스크에서_XX_상품_결제를_생성한다(String productName) {
        결제할_상품 = 상품_목록에서_상품이_조회된다(productName);

        결제_생성_응답 = 결제를_생성한다(
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
    }

    @Then("키오스크에서 결제 생성이 성공한다")
    public void 키오스크에서_결제_생성이_성공한다() {
        결제_생성이_성공한다(결제_생성_응답);
    }

    @When("키오스크에서 결제를 승인한다")
    public void 키오스크에서_결제를_승인한다() {
        var 결제_생성_결과 = 결제_생성_결과를_가져온다(결제_생성_응답);

        상품_결제_승인_응답 = 결제를_승인한다(
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
    }

    @Then("키오스크에서 결제 승인이 성공한다")
    public void 키오스크에서_결제_승인이_성공한다() {
        결제_승인이_성공한다(상품_결제_승인_응답);
    }
}
