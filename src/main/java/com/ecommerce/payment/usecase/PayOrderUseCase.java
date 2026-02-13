package com.ecommerce.payment.usecase;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.usecase.OrderRepository;
import com.ecommerce.payment.usecase.port.PaymentGateway;
import com.ecommerce.payment.usecase.event.PaymentSucceededEvent;
import com.ecommerce.shared.event.EventBus;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PayOrderUseCase {
    private final OrderRepository orderRepository;
    private final Map<String, PaymentGateway> paymentStrategies;
    private final EventBus eventBus;

    public PayOrderUseCase(OrderRepository orderRepository, Map<String, PaymentGateway> paymentStrategies, EventBus eventBus) {
        this.orderRepository = orderRepository;
        this.paymentStrategies = paymentStrategies;
        this.eventBus = eventBus;
    }

    public PayOrderOutput execute(PayOrderInput input) {
        Optional<Order> orderOpt = orderRepository.findById(input.orderId());
        if (orderOpt.isEmpty()) {
            return new PayOrderOutput(false, "Order not found");
        }
        Order order = orderOpt.get();

        if (order.getStatus() == Order.Status.PAID) {
            return new PayOrderOutput(false, "Order already paid");
        }

        PaymentGateway gateway = paymentStrategies.get(input.paymentMethod());
        if (gateway == null) {
            return new PayOrderOutput(false, "Invalid payment method");
        }

        boolean paymentSuccess = gateway.pay(order.getTotalAmount());
        if (paymentSuccess) {
            // Decoupling: Instead of updating order directly, publish event
            eventBus.publish(new PaymentSucceededEvent(order.getId(), order.getTotalAmount()));
            return new PayOrderOutput(true, "Payment successful via " + input.paymentMethod());
        } else {
            return new PayOrderOutput(false, "Payment failed");
        }
    }
}
