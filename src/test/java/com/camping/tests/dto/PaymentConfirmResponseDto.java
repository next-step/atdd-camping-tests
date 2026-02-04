package com.camping.tests.dto;

public record PaymentConfirmResponseDto(
        boolean success,
        String transactionId,
        String message,
        int paidAmount
) {
}
