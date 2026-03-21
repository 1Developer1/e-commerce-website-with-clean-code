package com.ecommerce.cart.usecase;

import com.ecommerce.cart.entity.Cart;

public record GetCartOutput(boolean success, String message, Cart cart) {
    public static GetCartOutput success(Cart cart) {
        return new GetCartOutput(true, "Success", cart);
    }

    public static GetCartOutput failure(String message) {
        return new GetCartOutput(false, message, null);
    }
}
