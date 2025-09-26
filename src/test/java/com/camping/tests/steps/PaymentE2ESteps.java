package com.camping.tests.steps;

import com.camping.tests.CommonContext;
import com.camping.tests.client.KioskClient;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentE2ESteps {

    private final KioskClient kioskClient = new KioskClient();

    @만약("키오스크에 결제 생성을 요청한다")
    public void 키오스크에_결제_생성을_요청한다() {
        Response response = kioskClient.createPayment();
        assertEquals(200, response.statusCode());
        assertEquals("true", CommonContext.isSuccess);
    }

    @그리고("키오스크에 결제 확정을 요청한다")
    public void 키오스크에_결제_확정을_요청한다() {
        Response response = kioskClient.confirmPayment();
        assertEquals(200, response.statusCode());
        assertEquals("true", CommonContext.isSuccess);
    }

    @그리고("키오스크에 금액 {int}원으로 결제 확정을 요청한다")
    public void 키오스크에_금액_원으로_결제_확정을_요청한다(int amount) {
        Response response = kioskClient.confirmPaymentWithAmount(amount);
        assertEquals(200, response.statusCode());
        assertEquals("true", CommonContext.isSuccess);
    }

    @그러면("결제가 성공이어야 한다")
    public void 결제가_성공이어야_한다() {
        assertEquals("true", CommonContext.isSuccess);
        assertEquals("결제 성공", CommonContext.responseMessage);
    }

    @그러면("결제가 실패이어야 한다")
    public void 결제가_실패이어야_한다() {
        assertEquals("false", CommonContext.isSuccess);
    }
}
