package com.ecommerce.cart.usecase.port;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.shared.domain.Money;
import java.util.Optional;

public interface DiscountProvider {
    Optional<Money> getDiscount(Cart cart, String code);
}
