package com.ecommerce.cart.adapter.in.controller;

import com.ecommerce.cart.usecase.AddToCartInput;
import com.ecommerce.cart.usecase.AddToCartOutput;
import com.ecommerce.cart.usecase.AddToCartUseCase;
import com.ecommerce.cart.usecase.ApplyDiscountInput;
import com.ecommerce.cart.usecase.ApplyDiscountOutput;
import com.ecommerce.cart.usecase.ApplyDiscountUseCase;

public class CartController {
    private final AddToCartUseCase addToCartUseCase;
    private final ApplyDiscountUseCase applyDiscountUseCase;

    public CartController(AddToCartUseCase addToCartUseCase, ApplyDiscountUseCase applyDiscountUseCase) {
        this.addToCartUseCase = addToCartUseCase;
        this.applyDiscountUseCase = applyDiscountUseCase;
    }

    public AddToCartOutput addToCart(AddToCartInput input) {
        return addToCartUseCase.execute(input);
    }

    public ApplyDiscountOutput applyDiscount(ApplyDiscountInput input) {
        return applyDiscountUseCase.execute(input);
    }
}
