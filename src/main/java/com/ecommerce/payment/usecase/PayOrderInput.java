package com.ecommerce.payment.usecase;

import java.util.UUID;

public record PayOrderInput(UUID orderId, String paymentMethod) {}
