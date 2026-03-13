package com.ecommerce.order.adapter.in.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.usecase.OrderRepository;
import com.ecommerce.payment.usecase.event.PaymentSucceededEvent;
import com.ecommerce.shared.event.EventBus;

import java.util.Optional;

public class OrderPaymentEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(OrderPaymentEventHandler.class);
    private final OrderRepository orderRepository;

    public OrderPaymentEventHandler(OrderRepository orderRepository, EventBus eventBus) {
        this.orderRepository = orderRepository;
        // Subscribe to event
        eventBus.subscribe(PaymentSucceededEvent.class, this::handlePaymentSuccess);
    }

    private void handlePaymentSuccess(PaymentSucceededEvent event) {
        Optional<Order> orderOpt = orderRepository.findById(event.orderId());
        orderOpt.ifPresent(order -> {
            order.pay();
            orderRepository.save(order);
            logger.info("[Event] Order " + order.getId() + " status updated to PAID.");
        });
    }
}
