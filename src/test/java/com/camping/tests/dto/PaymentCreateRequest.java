package com.camping.tests.dto;

import java.util.List;

public record PaymentCreateRequest(
        List<CartItem> items,
        String paymentMethod
) {
    public static PaymentCreateRequest card(List<CartItem> items) {
        return new PaymentCreateRequest(items, "CARD");
    }
}
