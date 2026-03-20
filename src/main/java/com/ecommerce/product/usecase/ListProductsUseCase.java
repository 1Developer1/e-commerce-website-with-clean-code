package com.ecommerce.product.usecase;

import com.ecommerce.product.entity.Product;
import java.util.List;
import java.util.stream.Collectors;

public class ListProductsUseCase {
    private final ProductRepository productRepository;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public ListProductsUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Paginated product listing.
     */
    public ListProductsOutput execute(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);

        List<Product> products = productRepository.findAll(safePage, safeSize);
        long totalElements = productRepository.count();

        List<ListProductsOutput.ProductSummary> summaries = products.stream()
                .map(p -> new ListProductsOutput.ProductSummary(
                        p.getId(), 
                        p.getName(), 
                        p.getPrice().getAmount(), 
                        p.getPrice().getCurrency()))
                .collect(Collectors.toList());
        return new ListProductsOutput(summaries, safePage, safeSize, totalElements);
    }

    /**
     * Default pagination (page 0, size 20).
     */
    public ListProductsOutput execute() {
        return execute(DEFAULT_PAGE, DEFAULT_SIZE);
    }
}
