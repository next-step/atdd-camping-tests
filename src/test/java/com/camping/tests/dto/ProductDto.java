package com.camping.tests.dto;

public record ProductDto(
        Long id,
        String name,
        int price,
        int stockQuantity,
        String productType
) {
}
