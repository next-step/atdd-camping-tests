package com.camping.tests.dto;

import java.util.List;

public record PaymentConfirmRequest(
        String paymentKey,
        String orderId,
        Integer amount,
        List<CartItem> items) {

    public static PaymentConfirmRequest defaultConfirm(String paymentKey, String orderId) {
        final CartItem defaultItem = CartItem.createDefault();
        return new PaymentConfirmRequest(paymentKey, orderId, defaultItem.unitPrice() * defaultItem.quantity(), List.of(defaultItem));
    }

    public static PaymentConfirmRequest withAmount(String paymentKey, String orderId, int amount) {
        return new PaymentConfirmRequest(paymentKey, orderId, amount, List.of(CartItem.createDefault()));
    }
}
