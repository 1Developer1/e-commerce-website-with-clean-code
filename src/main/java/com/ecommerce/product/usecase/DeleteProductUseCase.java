package com.ecommerce.product.usecase;

import java.util.UUID;
import java.util.Optional;
import com.ecommerce.product.entity.Product;

public class DeleteProductUseCase {
    private final ProductRepository productRepository;

    public DeleteProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public boolean execute(UUID id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
