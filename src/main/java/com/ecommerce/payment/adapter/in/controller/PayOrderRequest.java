package com.ecommerce.payment.adapter.in.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PayOrderRequest(
        @NotNull(message = "Order ID cannot be null")
        UUID orderId,

        @NotBlank(message = "Payment method cannot be blank")
        String method
) {}
