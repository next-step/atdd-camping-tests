package com.camping.tests.dto;

import java.util.List;

public record PaymentConfirmRequest(
        String paymentKey,
        String orderId,
        Integer amount,
        List<CartItem> items) {
}
