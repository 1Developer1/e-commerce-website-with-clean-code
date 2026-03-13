package com.ecommerce.order.adapter.in.controller;

import com.ecommerce.order.usecase.PlaceOrderInput;
import com.ecommerce.order.usecase.PlaceOrderOutput;
import com.ecommerce.order.usecase.PlaceOrderUseCase;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final PlaceOrderUseCase placeOrderUseCase;

    public OrderController(PlaceOrderUseCase placeOrderUseCase) {
        this.placeOrderUseCase = placeOrderUseCase;
    }

    @PostMapping
    public PlaceOrderOutput placeOrder(@RequestBody PlaceOrderInput input) {
        return placeOrderUseCase.execute(input);
    }
}
