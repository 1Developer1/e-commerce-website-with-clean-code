package com.ecommerce.order.usecase;

import java.util.UUID;

public record GetOrdersInput(UUID userId, int page, int size) {
}
