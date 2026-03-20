package com.ecommerce.cart.adapter.in.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AddToCartRequest(
        @NotNull(message = "Product ID cannot be null")
        UUID productId,
        
        @Positive(message = "Quantity must be greater than zero")
        int quantity
) {}
