package com.ecommerce.cart.adapter.in.controller;

import com.ecommerce.cart.usecase.AddToCartInput;
import com.ecommerce.cart.usecase.AddToCartOutput;
import com.ecommerce.cart.usecase.AddToCartUseCase;
import com.ecommerce.cart.usecase.ApplyDiscountInput;
import com.ecommerce.cart.usecase.ApplyDiscountOutput;
import com.ecommerce.cart.usecase.ApplyDiscountUseCase;
import com.ecommerce.cart.usecase.GetCartInput;
import com.ecommerce.cart.usecase.GetCartOutput;
import com.ecommerce.cart.usecase.GetCartUseCase;
import com.ecommerce.cart.adapter.in.presenter.CartPresenter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Cart", description = "Sepet yönetimi işlemleri")
@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
    private final GetCartUseCase getCartUseCase;
    private final AddToCartUseCase addToCartUseCase;
    private final ApplyDiscountUseCase applyDiscountUseCase;
    private final CartPresenter presenter;

    public CartController(GetCartUseCase getCartUseCase, AddToCartUseCase addToCartUseCase, ApplyDiscountUseCase applyDiscountUseCase, CartPresenter presenter) {
        this.getCartUseCase = getCartUseCase;
        this.addToCartUseCase = addToCartUseCase;
        this.applyDiscountUseCase = applyDiscountUseCase;
        this.presenter = presenter;
    }

    @Operation(summary = "Sepeti görüntüler", description = "Kullanıcının aktif sepetini döner.")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(@AuthenticationPrincipal UUID userId) {
        GetCartInput input = new GetCartInput(userId);
        GetCartOutput output = getCartUseCase.execute(input);
        return ResponseEntity.ok(presenter.presentGetCart(output));
    }

    @Operation(summary = "Sepete ürün ekler", description = "Belirtilen ürünü sepete ekler. Başarılı: 201 Created.")
    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addToCart(@AuthenticationPrincipal UUID userId, @Valid @RequestBody AddToCartRequest request) {
        AddToCartInput input = new AddToCartInput(userId, request.productId(), request.quantity());
        AddToCartOutput output = addToCartUseCase.execute(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(presenter.presentAddToCart(output));
    }

    @Operation(summary = "İndirim kuponu uygular", description = "Sepete indirim kuponu uygular.")
    @PostMapping("/discounts")
    public ResponseEntity<Map<String, Object>> applyDiscount(@AuthenticationPrincipal UUID userId, @Valid @RequestBody ApplyDiscountRequest request) {
        ApplyDiscountInput input = new ApplyDiscountInput(userId, request.code());
        ApplyDiscountOutput output = applyDiscountUseCase.execute(input);
        return ResponseEntity.ok(presenter.presentApplyDiscount(output));
    }
}
