package com.ecommerce.order.adapter.in.controller;

import com.ecommerce.order.usecase.PlaceOrderInput;
import com.ecommerce.order.usecase.PlaceOrderOutput;
import com.ecommerce.order.usecase.PlaceOrderUseCase;

public class OrderController {
    private final PlaceOrderUseCase placeOrderUseCase;

    public OrderController(PlaceOrderUseCase placeOrderUseCase) {
        this.placeOrderUseCase = placeOrderUseCase;
    }

    public PlaceOrderOutput placeOrder(PlaceOrderInput input) {
        return placeOrderUseCase.execute(input);
    }
}
