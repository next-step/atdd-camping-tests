package com.camping.tests.steps.kiosk.dto;

public record ProductDetail(
    int id,
    String name,
    int price,
    int stockQuantity,
    String productType
) {
}
