package com.ecommerce.cart.adapter.in.controller;

import com.ecommerce.cart.usecase.AddToCartInput;
import com.ecommerce.cart.usecase.AddToCartOutput;
import com.ecommerce.cart.usecase.AddToCartUseCase;

public class CartController {
    private final AddToCartUseCase addToCartUseCase;

    public CartController(AddToCartUseCase addToCartUseCase) {
        this.addToCartUseCase = addToCartUseCase;
    }

    public AddToCartOutput addToCart(AddToCartInput input) {
        return addToCartUseCase.execute(input);
    }
}
