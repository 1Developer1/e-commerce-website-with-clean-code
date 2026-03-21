package com.ecommerce.cart.usecase;

import com.ecommerce.cart.entity.Cart;
import java.util.Optional;

public class GetCartUseCase {
    private final CartRepository cartRepository;

    public GetCartUseCase(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public GetCartOutput execute(GetCartInput input) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(input.userId());
        Cart cart = cartOpt.orElseGet(() -> Cart.create(input.userId()));
        return GetCartOutput.success(cart);
    }
}
