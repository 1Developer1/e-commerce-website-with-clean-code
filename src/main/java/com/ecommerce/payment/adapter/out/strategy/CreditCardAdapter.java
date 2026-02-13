package com.ecommerce.payment.adapter.out.strategy;

import com.ecommerce.payment.usecase.port.PaymentGateway;
import com.ecommerce.shared.domain.Money;

public class CreditCardAdapter implements PaymentGateway {
    @Override
    public boolean pay(Money amount) {
        System.out.println("Processing Credit Card payment for " + amount);
        // Simulate external API call
        return true; 
    }
}
