package com.ecommerce.cart.usecase;

import java.math.BigDecimal;

public record AddToCartOutput(boolean success, String message, int itemsCount, BigDecimal totalAmount, String currency) {
}
