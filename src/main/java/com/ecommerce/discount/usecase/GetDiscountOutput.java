package com.ecommerce.discount.usecase;

import com.ecommerce.shared.domain.Money;

public record GetDiscountOutput(boolean isValid, String message, Money amount) {}
