package com.camping.tests.dto;

import java.util.List;

@SuppressWarnings("NonAsciiCharacters")
public record PaymentConfirmRequestDto(
        String paymentKey,
        String orderId,
        Integer amount,
        List<CartItemDto> items
) {
}
