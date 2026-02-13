package com.ecommerce.cart.usecase.dto;

import com.ecommerce.shared.domain.Money;
import java.util.UUID;

public record CartItemDto(UUID productId, int quantity, Money unitPrice, Money totalPrice) {}
