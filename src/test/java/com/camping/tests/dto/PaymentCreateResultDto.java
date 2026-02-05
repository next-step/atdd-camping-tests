package com.camping.tests.dto;

public record PaymentCreateResultDto(
        boolean success,
        String message,
        String paymentKey,
        String orderId,
        int amount
) {
}
