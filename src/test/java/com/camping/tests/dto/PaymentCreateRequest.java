package com.camping.tests.dto;

import java.util.List;

public record PaymentCreateRequest(List<CartItem> items, String paymentMethod) { // paymentMethod : CARD, CASH
    public static PaymentCreateRequest forCardPaymentWithDefaultItem() {
        return new PaymentCreateRequest(List.of(CartItem.createDefault()), "CARD");
    }
}
