package com.ecommerce.discount.adapter.out.persistence;

import com.ecommerce.discount.entity.Discount;
import com.ecommerce.discount.usecase.DiscountRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryDiscountRepository implements DiscountRepository {
    private final Map<UUID, Discount> store = new HashMap<>();

    @Override
    public void save(Discount discount) {
        store.put(discount.getId(), discount);
    }

    @Override
    public Optional<Discount> findByCode(String code) {
        return store.values().stream()
                .filter(d -> d.getCode().equals(code))
                .findFirst();
    }
}
