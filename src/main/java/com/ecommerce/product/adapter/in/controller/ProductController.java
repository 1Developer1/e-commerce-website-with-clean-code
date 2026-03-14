package com.ecommerce.product.adapter.in.controller;

import com.ecommerce.product.usecase.CreateProductInput;
import com.ecommerce.product.usecase.CreateProductUseCase;
import com.ecommerce.product.usecase.ListProductsUseCase;
import com.ecommerce.product.adapter.in.presenter.ProductPresenter;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final CreateProductUseCase createProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final ProductPresenter presenter;

    public ProductController(CreateProductUseCase createProductUseCase, ListProductsUseCase listProductsUseCase, ProductPresenter presenter) {
        this.createProductUseCase = createProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.presenter = presenter;
    }

    @PostMapping
    public Map<String, Object> createProduct(@RequestBody CreateProductInput input) {
        return presenter.presentCreateProduct(createProductUseCase.execute(input));
    }
    
    @GetMapping
    public Map<String, Object> listProducts() {
        return presenter.presentProductList(listProductsUseCase.execute());
    }
}
