package com.ecommerce.order.adapter.in.controller;

import com.ecommerce.order.usecase.PlaceOrderInput;
import com.ecommerce.order.usecase.PlaceOrderUseCase;
import com.ecommerce.order.adapter.in.presenter.OrderPresenter;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final PlaceOrderUseCase placeOrderUseCase;
    private final OrderPresenter presenter;

    public OrderController(PlaceOrderUseCase placeOrderUseCase, OrderPresenter presenter) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.presenter = presenter;
    }

    @PostMapping
    public Map<String, Object> placeOrder(@AuthenticationPrincipal UUID userId) {
        PlaceOrderInput input = new PlaceOrderInput(userId);
        return presenter.presentPlaceOrder(placeOrderUseCase.execute(input));
    }
}
