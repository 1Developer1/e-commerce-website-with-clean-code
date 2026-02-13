package com.ecommerce.payment.usecase.port;

import com.ecommerce.shared.domain.Money;

public interface PaymentGateway {
    boolean pay(Money amount);
}
