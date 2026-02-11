package com.camping.tests.factory;

import java.util.HashMap;
import java.util.Map;

public final class PaymentRequestFactory {

    private PaymentRequestFactory() {
    }

    public static Map<String, Object> defaultCartItemFixture() {
        return cartItemFixture(1L, "테스트상품", 10000, 1);
    }

    public static Map<String, Object> cartItemFixture(
            Long productId,
            String productName,
            Integer unitPrice,
            Integer quantity
    ) {
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("productId", productId);
        cartItem.put("productName", productName);
        cartItem.put("unitPrice", unitPrice);
        cartItem.put("quantity", quantity);
        return cartItem;
    }
}
