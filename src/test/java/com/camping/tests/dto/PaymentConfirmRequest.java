package com.camping.tests.dto;

import java.util.List;

import static com.camping.tests.common.TestConstants.DEFAULT_ORDER_ID;
import static com.camping.tests.common.TestConstants.DEFAULT_PAYMENT_KEY;

public record PaymentConfirmRequest(
        String paymentKey,
        String orderId,
        Integer amount,
        List<CartItem> items
) {

    public static PaymentConfirmRequest defaultConfirm() {
        final CartItem defaultItem = CartItem.createDefault();
        return new PaymentConfirmRequest(
                DEFAULT_PAYMENT_KEY,
                DEFAULT_ORDER_ID,
                defaultItem.unitPrice() * defaultItem.quantity(),
                List.of(defaultItem));
    }

    public static PaymentConfirmRequest withAmount(int amount) {
        return new PaymentConfirmRequest(
                DEFAULT_PAYMENT_KEY,
                DEFAULT_ORDER_ID,
                amount,
                List.of(CartItem.createDefault()));
    }
}
