package com.ecommerce.cart.usecase;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.usecase.dto.CartDto;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository {
    void save(Cart cart);
    Optional<Cart> findByUserId(UUID userId);
    Optional<CartDto> findDtoByUserId(UUID userId); // For other modules
}
