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
    private List<String> createdProductNames = new ArrayList<>();
    private List<Integer> createdProductIds = new ArrayList<>();
    private PaymentResult paymentResult;
    private Response paymentResponse;

    private int initialStockQuantity;

    @조건("Admin에 다음 상품이 등록되어 있다")
    public void registerProductsToAdmin(DataTable dataTable) {
        cartItems.clear();
        createdProductNames.clear();
        createdProductIds.clear();
        for (Map<String, String> product : dataTable.asMaps()) {
            String name = product.get("name");
            int price = Integer.parseInt(product.get("price"));

            AdminClient.ProductResult result = adminClient.createProduct(name, price);
            cartItems.add(CartItem.of(result.id(), price));
            createdProductNames.add(result.name());
            createdProductIds.add(result.id());
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
        response.then().body("$", hasSize(greaterThanOrEqualTo(count)));
    }

    @그리고("응답에 {string} 상품이 포함된다")
    public void verifyProductExists(String productName) {
        String actualName = createdProductNames.stream()
                .filter(name -> name.startsWith(productName))
                .findFirst()
                .orElse(productName);
        response.then().body("name", hasItem(actualName));
    }

    // ========== 재고 스텝 ==========

    @그리고("해당 상품의 초기 재고를 기억한다")
    public void rememberInitialStock() {
        int productId = createdProductIds.get(0);
        initialStockQuantity = adminClient.getProductStock(productId);
    }

    @그리고("Admin에서 해당 상품의 재고가 {int}만큼 감소해야 한다")
    public void verifyStockDecreased(int expectedDecrease) {
        int productId = createdProductIds.get(0);
        int currentStock = adminClient.getProductStock(productId);
        assertEquals(initialStockQuantity - expectedDecrease, currentStock,
                "재고가 " + expectedDecrease + "만큼 감소해야 합니다. 초기: " + initialStockQuantity + ", 현재: " + currentStock);
    }

    @그리고("Admin에서 해당 상품의 재고가 감소하지 않아야 한다")
    public void verifyStockUnchanged() {
        int productId = createdProductIds.get(0);
        int currentStock = adminClient.getProductStock(productId);
        assertEquals(initialStockQuantity, currentStock,
                "재고가 변경되지 않아야 합니다. 초기: " + initialStockQuantity + ", 현재: " + currentStock);
    }

    // ========== 매출 스텝 ==========

    @그리고("Admin 매출에 해당 상품의 판매 기록이 존재해야 한다")
    public void verifySalesRecordExists() {
        String productName = createdProductNames.get(0);
        Response salesResponse = adminClient.getSales();
        List<String> salesProductNames = salesResponse.jsonPath().getList("productName");
        assertTrue(salesProductNames.contains(productName),
                "매출에 상품 '" + productName + "'의 판매 기록이 존재해야 합니다. 매출 목록: " + salesProductNames);
    }

    @그리고("Admin 매출에 판매 기록이 추가되지 않아야 한다")
    public void verifySalesRecordNotAdded() {
        String productName = createdProductNames.get(0);
        Response salesResponse = adminClient.getSales();
        List<String> salesProductNames = salesResponse.jsonPath().getList("productName");
        assertFalse(salesProductNames.contains(productName),
                "매출에 상품 '" + productName + "'의 판매 기록이 없어야 합니다. 매출 목록: " + salesProductNames);
    }

    @그리고("Admin 매출에 해당 상품의 판매 기록이 {int}건이어야 한다")
    public void verifySalesRecordCount(int expectedCount) {
        String productName = createdProductNames.get(0);
        Response salesResponse = adminClient.getSales();
        List<String> salesProductNames = salesResponse.jsonPath().getList("productName");
        long count = salesProductNames.stream().filter(name -> name.equals(productName)).count();
        assertEquals(expectedCount, count,
                "매출에 상품 '" + productName + "'의 판매 기록이 " + expectedCount + "건이어야 합니다. 실제: " + count);
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

    @만약("동일 결제 정보로 다시 결제 확정을 요청한다")
    public void confirmPaymentAgain() {
        paymentResponse = kioskClient.confirmPayment(
                paymentResult.paymentKey(),
                paymentResult.orderId(),
                paymentResult.amount(),
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

    @그리고("결제 응답에 오류 메시지가 포함되어야 한다")
    public void verifyPaymentErrorMessage() {
        String body = paymentResponse.getBody().asString();
        assertNotNull(body, "결제 응답 본문이 null이면 안됩니다");
        assertTrue(body.contains("message") || body.contains("error") || body.contains("reason"),
                "결제 응답에 오류 메시지가 포함되어야 합니다. 응답: " + body);
    }
}