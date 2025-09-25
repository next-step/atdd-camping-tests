package com.camping.tests.steps.kiosk.dto;

public record KioskCreatePaymentResult(
    boolean success,
    String message,
    String paymentKey,
    String orderId,
    int amount
) {
}
