package com.camping.tests.dto;

import java.util.List;

public record PaymentCreateRequest(List<CartItem> items, String paymentMethod) { // paymentMethod : CARD, CASH
}
