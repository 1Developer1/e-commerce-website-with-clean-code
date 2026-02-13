package com.ecommerce.cart.usecase.port;

import com.ecommerce.shared.domain.Money;
import java.util.Optional;

public interface DiscountProvider {
    Optional<Money> getDiscount(String code);
}
