package com.camping.tests.steps.kiosk.dto;

public record KioskPaymentConfirmResult(
    boolean success,
    String transactionId,
    String message,
    int paidAmount
) {
}
