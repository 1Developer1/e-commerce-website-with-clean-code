package com.ecommerce.payment.adapter.in.controller;

import com.ecommerce.payment.usecase.PayOrderInput;
import com.ecommerce.payment.usecase.PayOrderOutput;
import com.ecommerce.payment.usecase.PayOrderUseCase;

public class PaymentController {
    private final PayOrderUseCase payOrderUseCase;

    public PaymentController(PayOrderUseCase payOrderUseCase) {
        this.payOrderUseCase = payOrderUseCase;
    }

    public PayOrderOutput payOrder(PayOrderInput input) {
        return payOrderUseCase.execute(input);
    }
}
