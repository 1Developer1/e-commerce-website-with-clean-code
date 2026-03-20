package com.ecommerce.shared.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * In-memory EventBus implementation with bounded subscriber capacity.
 * 
 * Back Pressure: Maximum subscriber count per event type is enforced.
 * If a new subscriber exceeds the limit, registration is rejected and logged.
 */
public class SimpleEventBus implements EventBus {
    private static final Logger logger = LoggerFactory.getLogger(SimpleEventBus.class);
    private static final int MAX_SUBSCRIBERS_PER_EVENT = 50;

    private final Map<Class<? extends DomainEvent>, List<Consumer<DomainEvent>>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void publish(DomainEvent event) {
        List<Consumer<DomainEvent>> handlers = subscribers.get(event.getClass());
        if (handlers != null) {
            for (Consumer<DomainEvent> handler : handlers) {
                try {
                    handler.accept(event);
                } catch (Exception e) {
                    logger.error("[EventBus] Handler failed for event {}: {}",
                            event.getClass().getSimpleName(), e.getMessage(), e);
                    // Let It Crash: Log and continue, don't kill the publisher
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers.compute(eventType, (key, existingHandlers) -> {
            if (existingHandlers == null) {
                existingHandlers = new ArrayList<>();
            }
            if (existingHandlers.size() >= MAX_SUBSCRIBERS_PER_EVENT) {
                logger.warn("[EventBus] Back Pressure: Max subscribers ({}) reached for event type {}. Registration rejected.",
                        MAX_SUBSCRIBERS_PER_EVENT, eventType.getSimpleName());
                return existingHandlers;
            }
            existingHandlers.add((Consumer<DomainEvent>) handler);
            return existingHandlers;
        });
    }
}
