package com.ecommerce.shared.event;

public interface EventBus {
    void publish(DomainEvent event);
    <T extends DomainEvent> void subscribe(Class<T> eventType, java.util.function.Consumer<T> handler);
}
