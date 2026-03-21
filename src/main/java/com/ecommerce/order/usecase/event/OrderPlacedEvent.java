package com.ecommerce.order.usecase.event;

import com.ecommerce.shared.event.DomainEvent;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record OrderPlacedEvent(UUID orderId, Map<UUID, Integer> productQuantities, LocalDateTime occurredOn) implements DomainEvent {
}
