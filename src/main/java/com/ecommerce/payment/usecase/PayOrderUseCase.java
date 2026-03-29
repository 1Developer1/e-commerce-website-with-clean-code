package com.ecommerce.payment.usecase;

import com.ecommerce.payment.usecase.port.PaymentGateway;
import com.ecommerce.payment.usecase.port.OrderQueryPort;
import com.ecommerce.payment.usecase.event.PaymentSucceededEvent;
import com.ecommerce.shared.domain.Money;
import com.ecommerce.shared.event.EventBus;

import java.util.Map;
import java.util.UUID;

public class PayOrderUseCase {
    private final Map<String, PaymentGateway> paymentStrategies;
    private final OrderQueryPort orderQueryPort;
    private final EventBus eventBus;

    public PayOrderUseCase(Map<String, PaymentGateway> paymentStrategies, OrderQueryPort orderQueryPort, EventBus eventBus) {
        this.paymentStrategies = paymentStrategies;
        this.orderQueryPort = orderQueryPort;
        this.eventBus = eventBus;
    }

    public PayOrderOutput execute(PayOrderInput input) {
        // Validate ownership: the requesting user must own the order
        UUID orderUserId = orderQueryPort.findOrderUserId(input.orderId()).orElse(null);
        if (orderUserId == null) {
            return new PayOrderOutput(false, "Order not found");
        }
        if (!orderUserId.equals(input.requestingUserId())) {
            return new PayOrderOutput(false, "Order does not belong to this user");
        }

        // Fetch the real amount from the order — never trust the client
        Money amount = orderQueryPort.findOrderTotal(input.orderId()).orElse(null);
        if (amount == null) {
            return new PayOrderOutput(false, "Order not found");
        }

        PaymentGateway gateway = paymentStrategies.get(input.paymentMethod());
        if (gateway == null) {
            return new PayOrderOutput(false, "Invalid payment method");
        }

        boolean paymentSuccess = gateway.pay(amount);
        if (paymentSuccess) {
            eventBus.publish(new PaymentSucceededEvent(input.orderId(), amount));
            return new PayOrderOutput(true, "Payment successful via " + input.paymentMethod());
        } else {
            return new PayOrderOutput(false, "Payment failed");
        }
    }
}
