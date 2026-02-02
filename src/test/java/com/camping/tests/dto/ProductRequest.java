package com.camping.tests.dto;

public record ProductRequest(
        String name,
        int stockQuantity,
        int price,
        String productType
) {
    public static ProductRequest sale(String name, int price) {
        return new ProductRequest(name, 10, price, "SALE");
    }
}