package com.ecommerce.cart.usecase;

import com.ecommerce.cart.entity.Cart;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository {
    Optional<Cart> findByUserId(UUID userId);
    void save(Cart cart);
}
