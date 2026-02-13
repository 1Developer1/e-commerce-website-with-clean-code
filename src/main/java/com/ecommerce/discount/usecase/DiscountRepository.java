package com.ecommerce.discount.usecase;

import com.ecommerce.discount.entity.Discount;
import java.util.Optional;

public interface DiscountRepository {
    void save(Discount discount);
    Optional<Discount> findByCode(String code);
}
