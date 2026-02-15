package com.ecommerce.shipping.adapter.in.event;

import com.ecommerce.payment.usecase.event.PaymentSucceededEvent;
import com.ecommerce.shared.event.EventBus;
import com.ecommerce.shipping.usecase.CreateShipmentUseCase;

public class OrderPaidEventHandler {
    private final CreateShipmentUseCase createShipmentUseCase;

    public OrderPaidEventHandler(CreateShipmentUseCase createShipmentUseCase, EventBus eventBus) {
        this.createShipmentUseCase = createShipmentUseCase;
        eventBus.subscribe(PaymentSucceededEvent.class, this::handle);
    }

    private void handle(PaymentSucceededEvent event) {
        System.out.println("[Shipping] Payment event received for Order: " + event.orderId());
        // In a real app, address would come from order details or event
        // For now, we hardcode a dummy address or assume it's part of the context
        String dummyAddress = "123 Clean Arch St, Code City";
        createShipmentUseCase.execute(event.orderId(), dummyAddress);
    }
}
