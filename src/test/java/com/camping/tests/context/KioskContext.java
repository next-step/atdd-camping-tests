package com.camping.tests.context;

import com.camping.tests.dto.CartItemDto;

import java.util.List;

public class KioskContext {
    private List<CartItemDto> cartItems;

    public void setCartItems(List<CartItemDto> cartItems) {
        this.cartItems = cartItems;
    }

    public List<CartItemDto> getCartItems() {
        return cartItems;
    }
}
