package com.camping.tests.dto;

public record CartItemDto(
        Long productId,
        String productName,
        int unitPrice,
        int quantity
) {
}
