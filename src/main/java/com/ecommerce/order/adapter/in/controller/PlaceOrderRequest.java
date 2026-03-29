package com.ecommerce.order.adapter.in.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaceOrderRequest(
        @NotBlank(message = "Recipient name is required")
        @Size(max = 255, message = "Recipient name must not exceed 255 characters")
        String recipientName,

        @NotBlank(message = "Shipping address is required")
        @Size(max = 1000, message = "Shipping address must not exceed 1000 characters")
        String shippingAddress
) {}
