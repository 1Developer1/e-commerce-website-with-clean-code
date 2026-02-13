package com.ecommerce.cart.usecase;

import java.util.UUID;

public record AddToCartInput(UUID userId, UUID productId, int quantity) {
}
