package com.ecommerce.payment.adapter.out.strategy;

import com.ecommerce.payment.usecase.port.PaymentGateway;
import com.ecommerce.shared.domain.Money;

public class BankTransferAdapter implements PaymentGateway {
    @Override
    public boolean pay(Money amount) {
        System.out.println("Processing Bank Transfer for " + amount);
        // Simulate bank API call
        return true; 
    }
}
