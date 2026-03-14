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

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final AddToCartUseCase addToCartUseCase;
    private final ApplyDiscountUseCase applyDiscountUseCase;

    public CartController(AddToCartUseCase addToCartUseCase, ApplyDiscountUseCase applyDiscountUseCase) {
        this.addToCartUseCase = addToCartUseCase;
        this.applyDiscountUseCase = applyDiscountUseCase;
    }

    public record AddToCartRequest(UUID productId, int quantity) {}

    @PostMapping("/add")
    public AddToCartOutput addToCart(@AuthenticationPrincipal UUID userId, @RequestBody AddToCartRequest request) {
        AddToCartInput input = new AddToCartInput(userId, request.productId(), request.quantity());
        return addToCartUseCase.execute(input);
    }

    public record ApplyDiscountRequest(String code) {}

    @PostMapping("/discount")
    public ApplyDiscountOutput applyDiscount(@AuthenticationPrincipal UUID userId, @RequestBody ApplyDiscountRequest request) {
        ApplyDiscountInput input = new ApplyDiscountInput(userId, request.code());
        return applyDiscountUseCase.execute(input);
    }
}
