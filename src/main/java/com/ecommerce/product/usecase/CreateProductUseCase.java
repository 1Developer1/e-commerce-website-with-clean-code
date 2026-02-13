package com.ecommerce.product.usecase;

import com.ecommerce.product.entity.Product;
import com.ecommerce.shared.domain.Money;

public class CreateProductUseCase {
    private final ProductRepository productRepository;

    public CreateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public CreateProductOutput execute(CreateProductInput input) {
        try {
            Money price = Money.of(input.priceAmount(), input.priceCurrency());
            Product product = Product.create(input.name(), input.description(), price, input.initialStock());
            productRepository.save(product);
            return new CreateProductOutput(product.getId(), product.getName(), true, "Product created successfully");
        } catch (IllegalArgumentException e) {
             return new CreateProductOutput(null, null, false, e.getMessage());
        }
    }
}
