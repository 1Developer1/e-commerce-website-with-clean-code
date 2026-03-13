package com.ecommerce.cart.adapter.in.controller;

import com.ecommerce.cart.usecase.AddToCartInput;
import com.ecommerce.cart.usecase.AddToCartOutput;
import com.ecommerce.cart.usecase.AddToCartUseCase;
import com.ecommerce.cart.usecase.ApplyDiscountInput;
import com.ecommerce.cart.usecase.ApplyDiscountOutput;
import com.ecommerce.cart.usecase.ApplyDiscountUseCase;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final AddToCartUseCase addToCartUseCase;
    private final ApplyDiscountUseCase applyDiscountUseCase;

    public CartController(AddToCartUseCase addToCartUseCase, ApplyDiscountUseCase applyDiscountUseCase) {
        this.addToCartUseCase = addToCartUseCase;
        this.applyDiscountUseCase = applyDiscountUseCase;
    }

    @PostMapping("/add")
    public AddToCartOutput addToCart(@RequestBody AddToCartInput input) {
        return addToCartUseCase.execute(input);
    }

    @PostMapping("/discount")
    public ApplyDiscountOutput applyDiscount(@RequestBody ApplyDiscountInput input) {
        return applyDiscountUseCase.execute(input);
    }
}
