package com.ecommerce.product.usecase;

import java.math.BigDecimal;

public record CreateProductInput(String name, String description, BigDecimal priceAmount, String priceCurrency, int initialStock) {
}
