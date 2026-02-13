package com.ecommerce.product.usecase;

import com.ecommerce.product.entity.Product;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface ProductRepository {
    void save(Product product);
    Optional<Product> findById(UUID id);
    List<Product> findAll();
}
