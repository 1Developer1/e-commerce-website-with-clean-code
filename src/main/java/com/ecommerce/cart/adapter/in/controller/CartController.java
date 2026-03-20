package com.ecommerce.cart.adapter.in.controller;

import com.ecommerce.cart.usecase.AddToCartInput;
import com.ecommerce.cart.usecase.AddToCartOutput;
import com.ecommerce.cart.usecase.AddToCartUseCase;
import com.ecommerce.cart.usecase.ApplyDiscountInput;
import com.ecommerce.cart.usecase.ApplyDiscountOutput;
import com.ecommerce.cart.usecase.ApplyDiscountUseCase;

import com.ecommerce.cart.adapter.in.presenter.CartPresenter;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final AddToCartUseCase addToCartUseCase;
    private final ApplyDiscountUseCase applyDiscountUseCase;
    private final CartPresenter presenter;

    public CartController(AddToCartUseCase addToCartUseCase, ApplyDiscountUseCase applyDiscountUseCase, CartPresenter presenter) {
        this.addToCartUseCase = addToCartUseCase;
        this.applyDiscountUseCase = applyDiscountUseCase;
        this.presenter = presenter;
    }

    @PostMapping("/add")
    public Map<String, Object> addToCart(@AuthenticationPrincipal UUID userId, @Valid @RequestBody AddToCartRequest request) {
        AddToCartInput input = new AddToCartInput(userId, request.productId(), request.quantity());
        AddToCartOutput output = addToCartUseCase.execute(input);
        return presenter.presentAddToCart(output);
    }

    @PostMapping("/discount")
    public Map<String, Object> applyDiscount(@AuthenticationPrincipal UUID userId, @Valid @RequestBody ApplyDiscountRequest request) {
        ApplyDiscountInput input = new ApplyDiscountInput(userId, request.code());
        ApplyDiscountOutput output = applyDiscountUseCase.execute(input);
        return presenter.presentApplyDiscount(output);
    }
}
