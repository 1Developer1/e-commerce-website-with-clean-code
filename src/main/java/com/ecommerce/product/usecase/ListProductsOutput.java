package com.ecommerce.product.usecase;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ListProductsOutput(List<ProductSummary> products) {
    public record ProductSummary(UUID id, String name, BigDecimal price, String currency) {}
}
