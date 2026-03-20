package com.ecommerce.payment.adapter.in.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record PayOrderRequest(
        @NotNull(message = "Order ID cannot be null")
        UUID orderId,
        
        @NotNull(message = "Amount cannot be null")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,
        
        @NotBlank(message = "Currency cannot be blank")
        String currency,
        
        @NotBlank(message = "Payment method cannot be blank")
        String method
) {}
