package com.ecommerce.payment.usecase;

import com.ecommerce.payment.usecase.port.PaymentGateway;
import com.ecommerce.payment.usecase.event.PaymentSucceededEvent;
import com.ecommerce.shared.event.EventBus;

import java.util.Map;

public class PayOrderUseCase {
    private final Map<String, PaymentGateway> paymentStrategies;
    private final EventBus eventBus;

    public PayOrderUseCase(Map<String, PaymentGateway> paymentStrategies, EventBus eventBus) {
        this.paymentStrategies = paymentStrategies;
        this.eventBus = eventBus;
    }

    public PayOrderOutput execute(PayOrderInput input) {
        PaymentGateway gateway = paymentStrategies.get(input.paymentMethod());
        if (gateway == null) {
            return new PayOrderOutput(false, "Invalid payment method");
        }

        // Use amount from input
        boolean paymentSuccess = gateway.pay(input.amount());
        if (paymentSuccess) {
            // Decoupling: Publish event. Order status update happens in EventHandler.
            eventBus.publish(new PaymentSucceededEvent(input.orderId(), input.amount()));
            return new PayOrderOutput(true, "Payment successful via " + input.paymentMethod());
        } else {
            return new PayOrderOutput(false, "Payment failed");
        }
    }
}
