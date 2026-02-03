package com.camping.tests.dto;

public record CartItem(Long productId, String productName, int unitPrice, int quantity) {
}
