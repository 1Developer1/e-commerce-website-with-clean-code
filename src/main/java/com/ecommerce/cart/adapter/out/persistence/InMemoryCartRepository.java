package com.ecommerce.cart.adapter.out.persistence;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.usecase.CartRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCartRepository implements CartRepository {
    private final Map<UUID, Cart> carts = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> userCartMap = new ConcurrentHashMap<>(); // UserId -> CartId

    @Override
    public Optional<Cart> findByUserId(UUID userId) {
        UUID cartId = userCartMap.get(userId);
        if (cartId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(carts.get(cartId));
    }

    @Override
    public void save(Cart cart) {
        carts.put(cart.getId(), cart);
        userCartMap.put(cart.getUserId(), cart.getId());
    }
}
