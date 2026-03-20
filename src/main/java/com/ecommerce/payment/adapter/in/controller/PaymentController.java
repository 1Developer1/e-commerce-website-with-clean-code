package com.ecommerce.payment.adapter.in.controller;

import com.ecommerce.payment.usecase.PayOrderInput;
import com.ecommerce.payment.usecase.PayOrderOutput;
import com.ecommerce.payment.usecase.PayOrderUseCase;

import com.ecommerce.payment.adapter.in.presenter.PaymentPresenter;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.ecommerce.shared.domain.Money;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PayOrderUseCase payOrderUseCase;
    private final PaymentPresenter presenter;

    public PaymentController(PayOrderUseCase payOrderUseCase, PaymentPresenter presenter) {
        this.payOrderUseCase = payOrderUseCase;
        this.presenter = presenter;
    }

    @PostMapping
    public Map<String, Object> payOrder(@AuthenticationPrincipal UUID userId, @Valid @RequestBody PayOrderRequest request) {
        Money money = Money.of(request.amount(), request.currency());
        PayOrderInput input = new PayOrderInput(request.orderId(), money, request.method());
        // Note: Currently PayOrderInput doesn't take userId, it assumes whoever pays is authenticated.
        PayOrderOutput output = payOrderUseCase.execute(input);
        return presenter.presentPayOrder(output);
    }
}
