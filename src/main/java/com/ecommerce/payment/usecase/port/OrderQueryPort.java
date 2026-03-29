package com.ecommerce.payment.usecase.port;

import com.ecommerce.shared.domain.Money;
import java.util.Optional;
import java.util.UUID;

public interface OrderQueryPort {
    Optional<Money> findOrderTotal(UUID orderId);
    Optional<UUID> findOrderUserId(UUID orderId);
}
