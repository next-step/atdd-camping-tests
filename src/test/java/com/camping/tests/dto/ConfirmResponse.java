package com.camping.tests.dto;

public record ConfirmResponse(
        String paymentKey,
        String orderId,
        String method,
        String approvedAt,
        Integer totalAmount,
        String status,
        Receipt receipt
) {
    public record Receipt(String url) {}
}
