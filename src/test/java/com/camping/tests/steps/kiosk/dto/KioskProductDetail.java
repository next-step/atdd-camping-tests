package com.camping.tests.steps.kiosk.dto;

public record KioskProductDetail(
    long id,
    String name,
    int price,
    int stockQuantity,
    String productType
) {
}
