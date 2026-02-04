package com.camping.tests.dto;

public record CartItem(Long productId, String productName, int unitPrice, int quantity) {
    public static CartItem createDefault() {
        return new CartItem(1L, "캠핑의자", 10000, 1);
    }
}
