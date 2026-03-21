package com.ecommerce.order.usecase;

import java.util.UUID;

public record GetOrderByIdInput(UUID userId, UUID orderId) {
}
