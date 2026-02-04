package com.camping.tests.dto;

import java.util.List;

public record PaymentConfirmRequestDto(
        String paymentKey,
        String orderId,
        Integer amount,
        List<CartItemDto> items
) {
}
