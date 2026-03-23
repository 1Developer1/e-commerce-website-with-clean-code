package com.ecommerce.product.adapter.in.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Validated request DTO for product update.
 * Partial update: null fields are ignored by the UseCase.
 */
public record UpdateProductRequest(
        @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Positive(message = "Price must be greater than zero")
        BigDecimal priceAmount,

        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
        String priceCurrency,

        @Positive(message = "Stock quantity must be greater than zero")
        Integer stockQuantity
) {}
