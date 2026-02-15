package com.ecommerce.payment.usecase;

import com.ecommerce.shared.domain.Money;
import java.util.UUID;

public record PayOrderInput(UUID orderId, Money amount, String paymentMethod) {}
