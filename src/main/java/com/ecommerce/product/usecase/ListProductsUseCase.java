package com.ecommerce.product.usecase;

import com.ecommerce.product.entity.Product;
import java.util.List;
import java.util.stream.Collectors;

public class ListProductsUseCase {
    private final ProductRepository productRepository;

    public ListProductsUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ListProductsOutput execute() {
        List<Product> products = productRepository.findAll();
        List<ListProductsOutput.ProductSummary> summaries = products.stream()
                .map(p -> new ListProductsOutput.ProductSummary(
                        p.getId(), 
                        p.getName(), 
                        p.getPrice().getAmount(), 
                        p.getPrice().getCurrency()))
                .collect(Collectors.toList());
        return new ListProductsOutput(summaries);
    }
}
