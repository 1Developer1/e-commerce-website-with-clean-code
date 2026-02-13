package com.ecommerce.cart.usecase;

import java.util.UUID;

public record ApplyDiscountInput(UUID userId, String discountCode) {}
