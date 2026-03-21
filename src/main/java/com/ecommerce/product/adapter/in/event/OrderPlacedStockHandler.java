package com.ecommerce.product.adapter.in.event;

import com.ecommerce.order.usecase.event.OrderPlacedEvent;
import com.ecommerce.product.usecase.DeductProductStockUseCase;
import com.ecommerce.shared.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class OrderPlacedStockHandler {

    private static final Logger logger = LoggerFactory.getLogger(OrderPlacedStockHandler.class);
    
    private final EventBus eventBus;
    private final DeductProductStockUseCase deductProductStockUseCase;

    public OrderPlacedStockHandler(EventBus eventBus, DeductProductStockUseCase deductProductStockUseCase) {
        this.eventBus = eventBus;
        this.deductProductStockUseCase = deductProductStockUseCase;
    }

    @PostConstruct
    public void registerHandler() {
        eventBus.subscribe(OrderPlacedEvent.class, this::handleOrderPlacedEvent);
        logger.info("OrderPlacedStockHandler subscribed to OrderPlacedEvent");
    }

    private void handleOrderPlacedEvent(OrderPlacedEvent event) {
        try {
            logger.info("Handling OrderPlacedEvent for Order ID: {}", event.orderId());
            deductProductStockUseCase.execute(event.productQuantities());
            logger.info("Successfully deducted stock for Order ID: {}", event.orderId());
        } catch (Exception e) {
            logger.error("Failed to process stock deduction for Order ID: {}", event.orderId(), e);
            // Optionally dispatch a Compensating Transaction event, e.g., OrderFailedDueToStockEvent
        }
    }
}
