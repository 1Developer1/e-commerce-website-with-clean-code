package com.ecommerce.payment.usecase;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.usecase.OrderRepository;
import com.ecommerce.payment.usecase.port.PaymentGateway;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PayOrderUseCase {
    private final OrderRepository orderRepository;
    private final Map<String, PaymentGateway> paymentStrategies;

    public PayOrderUseCase(OrderRepository orderRepository, Map<String, PaymentGateway> paymentStrategies) {
        this.orderRepository = orderRepository;
        this.paymentStrategies = paymentStrategies;
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
            order.pay();
            orderRepository.save(order);
            return new PayOrderOutput(true, "Payment successful via " + input.paymentMethod());
        } else {
            return new PayOrderOutput(false, "Payment failed");
        }
    }
}
