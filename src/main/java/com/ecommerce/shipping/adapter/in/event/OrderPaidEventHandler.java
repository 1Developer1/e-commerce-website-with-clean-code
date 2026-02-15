package com.ecommerce.shipping.adapter.in.event;

import com.ecommerce.payment.usecase.event.PaymentSucceededEvent;
import com.ecommerce.shared.event.EventBus;
import com.ecommerce.shipping.api.ShippingService;

public class OrderPaidEventHandler {
    private final ShippingService shippingService;

    public OrderPaidEventHandler(ShippingService shippingService, EventBus eventBus) {
        this.shippingService = shippingService;
        eventBus.subscribe(PaymentSucceededEvent.class, this::handle);
    }

    private void handle(PaymentSucceededEvent event) {
        System.out.println("[Shipping] Payment event received for Order: " + event.orderId());
        // In a real app, address would come from order details or event
        // For now, we hardcode a dummy address or assume it's part of the context
        String dummyAddress = "123 Clean Arch St, Code City";
        shippingService.createShipment(event.orderId(), dummyAddress);
    }
}
