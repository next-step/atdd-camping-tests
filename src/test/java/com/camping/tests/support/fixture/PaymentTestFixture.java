package com.camping.tests.support.fixture;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.camping.tests.support.client.ApiClientFactory;
import static org.assertj.core.api.Assertions.assertThat;

public class PaymentTestFixture {

    public static ExtractableResponse<Response> 정상_금액으로_결제_요청(List<Map<String, Object>> selectedItems) {
        List<Map<String, Object>> cartItems = new ArrayList<>();
        for (Map<String, Object> item : selectedItems) {
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("productId", item.get("productId"));
            cartItem.put("productName", "Test Product " + item.get("productId"));
            cartItem.put("unitPrice", item.get("price"));
            cartItem.put("quantity", item.get("quantity"));
            cartItems.add(cartItem);
        }

        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("items", cartItems);
        paymentRequest.put("paymentMethod", "CARD");

        return ApiClientFactory.kiosk().post("/api/payments", paymentRequest);
    }

    public static ExtractableResponse<Response> 유효하지_않은_금액으로_결제_요청() {
        List<Map<String, Object>> cartItems = new ArrayList<>();
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("productId", 998L);
        cartItem.put("productName", "Zero Price Product");
        cartItem.put("unitPrice", 0); // 0원으로 설정하여 에러 유발
        cartItem.put("quantity", 1);
        cartItems.add(cartItem);

        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("items", cartItems);
        paymentRequest.put("paymentMethod", "CARD");

        return ApiClientFactory.kiosk().post("/api/payments", paymentRequest);
    }

    public static void 결제_성공_검증(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("success")).isTrue();
    }

    public static void 결제_실패_검증(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("success")).isFalse();
    }

    public static void paymentKey_포함_검증(ExtractableResponse<Response> response) {
        String paymentKey = response.jsonPath().getString("paymentKey");
        assertThat(paymentKey).isNotNull();
        assertThat(paymentKey).isNotEmpty();
    }

    public static void orderId_포함_검증(ExtractableResponse<Response> response) {
        String orderId = response.jsonPath().getString("orderId");
        assertThat(orderId).isNotNull();
    }

    public static void 실패_메시지_검증(ExtractableResponse<Response> response, String expectedMessage) {
        String actualMessage = response.jsonPath().getString("message");
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    public static List<Map<String, Object>> 상품_목록_생성(List<Map<String, String>> items) {
        List<Map<String, Object>> selectedItems = new ArrayList<>();
        for (Map<String, String> item : items) {
            Map<String, Object> selectedItem = new HashMap<>();
            selectedItem.put("productId", Long.parseLong(item.get("productId")));
            selectedItem.put("quantity", Integer.parseInt(item.get("quantity")));
            selectedItem.put("price", Integer.parseInt(item.get("price")));
            selectedItems.add(selectedItem);
        }
        return selectedItems;
    }
}