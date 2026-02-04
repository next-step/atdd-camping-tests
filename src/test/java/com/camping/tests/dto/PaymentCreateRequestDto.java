package com.camping.tests.dto;

import java.util.List;

public record PaymentCreateRequestDto(
        List<CartItemDto> items,
        String paymentMethod
) {
}
