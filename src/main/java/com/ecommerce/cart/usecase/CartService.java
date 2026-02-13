package com.ecommerce.cart.usecase;

import com.ecommerce.cart.usecase.dto.CartDto;
import java.util.Optional;
import java.util.UUID;

public interface CartService {
    Optional<CartDto> getCartForOrder(UUID userId);
    void clearCart(UUID userId);
}
