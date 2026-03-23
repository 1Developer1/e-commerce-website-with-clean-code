package com.ecommerce.product.usecase;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.usecase.dto.ProductResponse;
import com.ecommerce.shared.domain.Money;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class UpdateProductUseCase {
    private final ProductRepository productRepository;

    public UpdateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse execute(UUID id, String name, String description, BigDecimal price, String currency, Integer stockQuantity) {
        Optional<Product> prodOpt = productRepository.findById(id);
        if (prodOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }
        
        Product product = prodOpt.get();
        Money newPrice = (price != null && currency != null) ? Money.of(price, currency) : null;
        product.update(name, description, newPrice, stockQuantity);
        
        productRepository.save(product);
        
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice().getAmount(),
            product.getPrice().getCurrency(),
            product.getStockQuantity()
        );
    }
}
