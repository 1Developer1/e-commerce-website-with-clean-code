package com.ecommerce.order.usecase;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceOrderOutput(boolean success, String message, UUID orderId, String status, BigDecimal totalAmount) {
}
