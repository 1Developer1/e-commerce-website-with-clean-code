package com.ecommerce.product.adapter.in.controller;

import com.ecommerce.product.usecase.CreateProductInput;
import com.ecommerce.product.usecase.CreateProductOutput;
import com.ecommerce.product.usecase.CreateProductUseCase;
import com.ecommerce.product.usecase.ListProductsOutput;
import com.ecommerce.product.usecase.ListProductsUseCase;

public class ProductController {
    private final CreateProductUseCase createProductUseCase;
    private final ListProductsUseCase listProductsUseCase;

    public ProductController(CreateProductUseCase createProductUseCase, ListProductsUseCase listProductsUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
    }

    public CreateProductOutput createProduct(CreateProductInput input) {
        return createProductUseCase.execute(input);
    }

    public ListProductsOutput listProducts() {
        return listProductsUseCase.execute();
    }
}
