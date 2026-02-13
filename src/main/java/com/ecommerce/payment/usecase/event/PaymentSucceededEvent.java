package com.ecommerce.payment.usecase.event;

import com.ecommerce.shared.event.DomainEvent;
import com.ecommerce.shared.domain.Money;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentSucceededEvent(UUID orderId, Money amount, LocalDateTime occurredOn) implements DomainEvent {
    public PaymentSucceededEvent(UUID orderId, Money amount) {
        this(orderId, amount, LocalDateTime.now());
    }
}
