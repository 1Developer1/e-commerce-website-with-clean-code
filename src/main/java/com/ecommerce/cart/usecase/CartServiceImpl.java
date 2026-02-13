package com.ecommerce.cart.usecase;

import com.ecommerce.cart.usecase.dto.CartDto;
import java.util.Optional;
import java.util.UUID;

public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;

    public CartServiceImpl(CartRepository cartRepository) {
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
