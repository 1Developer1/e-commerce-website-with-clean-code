package com.ecommerce.order.adapter.in.controller;

import com.ecommerce.order.usecase.GetOrdersInput;
import com.ecommerce.order.usecase.GetOrdersUseCase;
import com.ecommerce.order.usecase.GetOrderByIdInput;
import com.ecommerce.order.usecase.GetOrderByIdUseCase;
import com.ecommerce.order.usecase.PlaceOrderInput;
import com.ecommerce.order.usecase.PlaceOrderUseCase;
import com.ecommerce.order.adapter.in.presenter.OrderPresenter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Orders", description = "Sipariş oluşturma ve sorgulama")
@RestController
@RequestMapping("/api/v1/orders")
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

    @Operation(summary = "Sipariş oluşturur", description = "Aktif sepetten sipariş oluşturur. Başarılı: 201 Created.")
    @PostMapping
    public ResponseEntity<Map<String, Object>> placeOrder(@AuthenticationPrincipal UUID userId) {
        PlaceOrderInput input = new PlaceOrderInput(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(presenter.presentPlaceOrder(placeOrderUseCase.execute(input)));
    }

    @Operation(summary = "Siparişleri listeler", description = "Kullanıcının siparişlerini sayfalanmış olarak döner.")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrders(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        GetOrdersInput input = new GetOrdersInput(userId, page, size);
        return ResponseEntity.ok(presenter.presentGetOrders(getOrdersUseCase.execute(input)));
    }

    @Operation(summary = "Sipariş detayını getirir", description = "Belirtilen ID'ye ait sipariş detayını döner.")
    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderById(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID orderId) {
        GetOrderByIdInput input = new GetOrderByIdInput(userId, orderId);
        var output = getOrderByIdUseCase.execute(input);
        if (!output.success()) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(presenter.presentGetOrder(output));
        }
        return ResponseEntity.ok(presenter.presentGetOrder(output));
    }
}
