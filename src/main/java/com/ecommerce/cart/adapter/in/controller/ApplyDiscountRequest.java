package com.ecommerce.cart.adapter.in.controller;

import jakarta.validation.constraints.NotBlank;

public record ApplyDiscountRequest(
        @NotBlank(message = "Discount code cannot be blank")
        String code
) {}
