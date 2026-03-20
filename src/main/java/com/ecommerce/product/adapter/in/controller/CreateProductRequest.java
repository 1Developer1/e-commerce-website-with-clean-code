package com.ecommerce.product.adapter.in.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Validated request DTO for product creation.
 * Jakarta Bean Validation (JSR-380) annotations enforce input constraints
 * at the Controller boundary, before reaching the UseCase layer.
 */
public record CreateProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than zero")
        BigDecimal priceAmount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
        String priceCurrency,

        @Positive(message = "Initial stock must be greater than zero")
        int initialStock
) {}
