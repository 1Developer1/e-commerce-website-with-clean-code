package com.ecommerce.product.adapter.out.persistence;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.usecase.ProductRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProductRepository implements ProductRepository {
    private final Map<UUID, Product> products = new ConcurrentHashMap<>();

    @Override
    public void save(Product product) {
        products.put(product.getId(), product);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }
}
