package com.ecommerce.cart.internal;

import com.ecommerce.cart.api.CartService;
import com.ecommerce.cart.usecase.CartRepository;
import com.ecommerce.cart.usecase.dto.CartDto;
import java.util.Optional;
import java.util.UUID;

class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;

    CartServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public Optional<CartDto> getCartForOrder(UUID userId) {
        return cartRepository.findDtoByUserId(userId);
    }

    @Override
    public void clearCart(UUID userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.clear();
            cartRepository.save(cart);
        });
    }
}
