package com.camping.tests.dto;

public record CartItem(int productId, int quantity, int unitPrice) {
    public static CartItem of(int productId, int price) {
        return new CartItem(productId, 1, price);
    }
}
