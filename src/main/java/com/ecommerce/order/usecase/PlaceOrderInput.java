package com.ecommerce.order.usecase;

import java.util.UUID;

public record PlaceOrderInput(UUID userId, String recipientName, String shippingAddress) {
}
