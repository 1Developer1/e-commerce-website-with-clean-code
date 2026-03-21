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

    @Override
    public List<Product> findAll(int page, int size) {
        List<Product> all = new ArrayList<>(products.values());
        int fromIndex = page * size;
        if (fromIndex >= all.size()) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + size, all.size());
        return all.subList(fromIndex, toIndex);
    }

    @Override
    public long count() {
        return products.size();
    }

    @Override
    public void deleteById(UUID id) {
        products.remove(id);
    }
}
