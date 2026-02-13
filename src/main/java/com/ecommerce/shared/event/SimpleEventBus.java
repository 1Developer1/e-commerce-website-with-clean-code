package com.ecommerce.shared.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SimpleEventBus implements EventBus {
    private final Map<Class<? extends DomainEvent>, List<Consumer<DomainEvent>>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void publish(DomainEvent event) {
        List<Consumer<DomainEvent>> handlers = subscribers.get(event.getClass());
        if (handlers != null) {
            handlers.forEach(handler -> handler.accept(event));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>())
                   .add((Consumer<DomainEvent>) handler);
    }
}
