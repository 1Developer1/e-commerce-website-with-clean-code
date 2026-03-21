package com.ecommerce.order.adapter.in.controller;

import com.ecommerce.order.usecase.GetOrdersInput;
import com.ecommerce.order.usecase.GetOrdersUseCase;
import com.ecommerce.order.usecase.GetOrderByIdInput;
import com.ecommerce.order.usecase.GetOrderByIdUseCase;
import com.ecommerce.order.usecase.PlaceOrderInput;
import com.ecommerce.order.usecase.PlaceOrderUseCase;
import com.ecommerce.order.adapter.in.presenter.OrderPresenter;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final PlaceOrderUseCase placeOrderUseCase;
    private final GetOrdersUseCase getOrdersUseCase;
    private final GetOrderByIdUseCase getOrderByIdUseCase;
    private final OrderPresenter presenter;

    public OrderController(PlaceOrderUseCase placeOrderUseCase, 
                           GetOrdersUseCase getOrdersUseCase, 
                           GetOrderByIdUseCase getOrderByIdUseCase, 
                           OrderPresenter presenter) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.getOrdersUseCase = getOrdersUseCase;
        this.getOrderByIdUseCase = getOrderByIdUseCase;
        this.presenter = presenter;
    }

    @PostMapping
    public Map<String, Object> placeOrder(@AuthenticationPrincipal UUID userId) {
        PlaceOrderInput input = new PlaceOrderInput(userId);
        return presenter.presentPlaceOrder(placeOrderUseCase.execute(input));
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrders(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        GetOrdersInput input = new GetOrdersInput(userId, page, size);
        return ResponseEntity.ok(presenter.presentGetOrders(getOrdersUseCase.execute(input)));
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderById(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID orderId) {
        GetOrderByIdInput input = new GetOrderByIdInput(userId, orderId);
        var output = getOrderByIdUseCase.execute(input);
        if (!output.success()) {
             return ResponseEntity.status(404).body(presenter.presentGetOrder(output));
        }
        return ResponseEntity.ok(presenter.presentGetOrder(output));
    }
}
