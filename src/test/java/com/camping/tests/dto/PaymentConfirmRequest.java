package com.camping.tests.dto;

import java.util.List;

public record PaymentConfirmRequest(
        String paymentKey,
        String orderId,
        int amount,
        List<CartItem> items
) {
}
