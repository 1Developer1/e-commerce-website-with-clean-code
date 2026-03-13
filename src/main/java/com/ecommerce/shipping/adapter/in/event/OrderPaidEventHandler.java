package com.ecommerce.shipping.adapter.in.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecommerce.payment.usecase.event.PaymentSucceededEvent;
import com.ecommerce.shared.event.EventBus;
import com.ecommerce.shipping.api.ShippingService;

public class OrderPaidEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(OrderPaidEventHandler.class);
    private final ShippingService shippingService;

    public OrderPaidEventHandler(ShippingService shippingService, EventBus eventBus) {
        this.shippingService = shippingService;
        eventBus.subscribe(PaymentSucceededEvent.class, this::handle);
    }

    private void handle(PaymentSucceededEvent event) {
        logger.info("[Shipping] Payment event received for Order: " + event.orderId());
        // In a real app, address would come from order details or event
        // For now, we hardcode a dummy address or assume it's part of the context
        String dummyAddress = "123 Clean Arch St, Code City";
        shippingService.createShipment(event.orderId(), dummyAddress);
    }
}
