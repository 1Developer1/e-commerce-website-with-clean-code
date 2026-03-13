package com.ecommerce.payment.adapter.in.controller;

import com.ecommerce.payment.usecase.PayOrderInput;
import com.ecommerce.payment.usecase.PayOrderOutput;
import com.ecommerce.payment.usecase.PayOrderUseCase;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PayOrderUseCase payOrderUseCase;

    public PaymentController(PayOrderUseCase payOrderUseCase) {
        this.payOrderUseCase = payOrderUseCase;
    }

    @PostMapping
    public PayOrderOutput payOrder(@RequestBody PayOrderInput input) {
        return payOrderUseCase.execute(input);
    }
}
