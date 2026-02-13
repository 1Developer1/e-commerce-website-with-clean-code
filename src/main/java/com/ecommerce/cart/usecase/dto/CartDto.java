package com.ecommerce.cart.usecase.dto;

import com.ecommerce.shared.domain.Money;
import java.util.List;
import java.util.UUID;

public record CartDto(UUID userId, List<CartItemDto> items, Money discount, Money totalAmount) {
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}
