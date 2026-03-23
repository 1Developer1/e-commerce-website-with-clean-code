package com.ecommerce.payment.adapter.in.controller;

import com.ecommerce.payment.usecase.PayOrderInput;
import com.ecommerce.payment.usecase.PayOrderOutput;
import com.ecommerce.payment.usecase.PayOrderUseCase;
import com.ecommerce.payment.adapter.in.presenter.PaymentPresenter;
import com.ecommerce.shared.domain.Money;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Payments", description = "Ödeme işlemleri (Strategy: Kredi Kartı / Havale)")
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PayOrderUseCase payOrderUseCase;
    private final PaymentPresenter presenter;

    public PaymentController(PayOrderUseCase payOrderUseCase, PaymentPresenter presenter) {
        this.payOrderUseCase = payOrderUseCase;
        this.presenter = presenter;
    }

    @Operation(summary = "Siparişi öder", description = "Belirtilen ödeme yöntemiyle sipariş ödemesi yapar.")
    @PostMapping
    public ResponseEntity<Map<String, Object>> payOrder(@AuthenticationPrincipal UUID userId, @Valid @RequestBody PayOrderRequest request) {
        Money money = Money.of(request.amount(), request.currency());
        PayOrderInput input = new PayOrderInput(request.orderId(), money, request.method());
        PayOrderOutput output = payOrderUseCase.execute(input);

        if (!output.success()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(presenter.presentPayOrder(output));
        }
        return ResponseEntity.ok(presenter.presentPayOrder(output));
    }
}
