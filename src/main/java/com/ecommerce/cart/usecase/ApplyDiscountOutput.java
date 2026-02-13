package com.ecommerce.cart.usecase;

import com.ecommerce.shared.domain.Money;

public record ApplyDiscountOutput(boolean success, String message, Money newTotal) {}
