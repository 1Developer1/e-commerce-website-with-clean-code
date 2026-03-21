package com.ecommerce.product.usecase;

public record ProductResponse(String id, String name, String description, String displayPrice, Integer stockQuantity) {}
