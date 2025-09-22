package com.camping.tests.steps.admin.dto;

public record AdminProductDetail(
    long id,
    String name,
    String description,
    String productType,
    int price,
    int stockQuantity
) {
}
