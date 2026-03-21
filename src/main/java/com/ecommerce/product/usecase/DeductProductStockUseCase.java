package com.ecommerce.product.usecase;

import com.ecommerce.product.entity.Product;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DeductProductStockUseCase {
    private final ProductRepository productRepository;

    public DeductProductStockUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void execute(Map<UUID, Integer> productQuantities) {
        for (Map.Entry<UUID, Integer> entry : productQuantities.entrySet()) {
            UUID productId = entry.getKey();
            Integer quantity = entry.getValue();

            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                try {
                    product.decreaseStock(quantity);
                    productRepository.save(product);
                } catch (IllegalStateException e) {
                    // In a more complex domain, Handle OutOfStock scenarios (e.g. Compensating transaction or alerting)
                    // For now, if stock is insufficient we simply don't deduct below zero or throw exception. 
                    // Let's throw a runtime exception or log it; to maintain data integrity we should fail fast.
                    throw new IllegalStateException("Not enough stock for product " + productId);
                }
            } else {
                throw new IllegalStateException("Product not found " + productId);
            }
        }
    }
}
