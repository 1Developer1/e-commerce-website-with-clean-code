package com.ecommerce.cart.internal;

import com.ecommerce.cart.api.CartService;
import com.ecommerce.cart.usecase.CartRepository;

public class CartModule {
    public static CartService createService(CartRepository cartRepository) {
        return new CartServiceImpl(cartRepository);
    }
}
