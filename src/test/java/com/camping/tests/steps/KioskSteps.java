package com.camping.tests.steps;

import com.camping.tests.dto.CartItem;
import com.camping.tests.support.AdminClient;
import com.camping.tests.support.KioskClient;
import com.camping.tests.support.KioskClient.PaymentResult;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.조건;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class KioskSteps {

    private final AdminClient adminClient = new AdminClient();
    private final KioskClient kioskClient = new KioskClient();

    private Response response;
    private List<CartItem> cartItems = new ArrayList<>();
    private PaymentResult paymentResult;
    private Response paymentResponse;

    @조건("Admin에 다음 상품이 등록되어 있다")
    public void registerProductsToAdmin(DataTable dataTable) {
        cartItems.clear();
        for (Map<String, String> product : dataTable.asMaps()) {
            String name = product.get("name");
            int price = Integer.parseInt(product.get("price"));

            int productId = adminClient.createProduct(name, price);
            cartItems.add(CartItem.of(productId, price));
        }
    }

    @만약("Kiosk에서 상품 목록을 조회한다")
    public void getProductsFromKiosk() {
        response = kioskClient.getProducts();
    }

    @그러면("응답 상태 코드는 {int}이다")
    public void verifyStatusCode(int statusCode) {
        assertEquals(statusCode, response.getStatusCode());
    }

    @그리고("응답에 {int}개의 상품이 포함된다")
    public void verifyProductCount(int count) {
        response.then().body("$", hasSize(count));
    }

    @그리고("응답에 {string} 상품이 포함된다")
    public void verifyProductExists(String productName) {
        response.then().body("name", hasItem(productName));
    }

    // ========== 결제 스텝 ==========

    @만약("키오스크에 결제 생성을 요청한다")
    public void createPayment() {
        paymentResult = kioskClient.createPayment(cartItems);
    }

    @그리고("키오스크에 결제 확정을 요청한다")
    public void confirmPayment() {
        paymentResponse = kioskClient.confirmPayment(
                paymentResult.paymentKey(),
                paymentResult.orderId(),
                paymentResult.amount(),
                cartItems
        );
    }

    @그리고("키오스크에 금액 {int}원으로 결제 확정을 요청한다")
    public void confirmPaymentWithWrongAmount(int wrongAmount) {
        paymentResponse = kioskClient.confirmPayment(
                paymentResult.paymentKey(),
                paymentResult.orderId(),
                wrongAmount,
                cartItems
        );
    }

    @그러면("결제가 성공이어야 한다")
    public void verifyPaymentSuccess() {
        assertTrue(paymentResponse.jsonPath().getBoolean("success"),
                "결제가 성공해야 합니다. 응답: " + paymentResponse.getBody().asString());
    }

    @그러면("결제가 실패이어야 한다")
    public void verifyPaymentFailure() {
        assertFalse(paymentResponse.jsonPath().getBoolean("success"),
                "결제가 실패해야 합니다. 응답: " + paymentResponse.getBody().asString());
    }
}