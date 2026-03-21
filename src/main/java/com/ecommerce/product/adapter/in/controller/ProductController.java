package com.ecommerce.product.adapter.in.controller;

import com.ecommerce.product.usecase.CreateProductInput;
import com.ecommerce.product.usecase.CreateProductOutput;
import com.ecommerce.product.usecase.CreateProductUseCase;
import com.ecommerce.product.usecase.ListProductsOutput;
import com.ecommerce.product.usecase.ListProductsUseCase;
import com.ecommerce.product.usecase.UpdateProductUseCase;
import com.ecommerce.product.usecase.DeleteProductUseCase;
import com.ecommerce.product.usecase.ProductResponse;
import com.ecommerce.product.adapter.in.presenter.ProductPresenter;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final CreateProductUseCase createProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final ProductPresenter presenter;

    public ProductController(CreateProductUseCase createProductUseCase, ListProductsUseCase listProductsUseCase, 
                             UpdateProductUseCase updateProductUseCase,
                             DeleteProductUseCase deleteProductUseCase,
                             ProductPresenter presenter) {
        this.createProductUseCase = createProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.presenter = presenter;
    }

    @PostMapping
    public Map<String, Object> createProduct(@Valid @RequestBody CreateProductRequest request) {
        CreateProductInput input = new CreateProductInput(
                request.name(), request.description(),
                request.priceAmount(), request.priceCurrency(),
                request.initialStock()
        );
        return presenter.presentCreateProduct(createProductUseCase.execute(input));
    }
    
    @GetMapping
    public Map<String, Object> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return presenter.presentProductList(listProductsUseCase.execute(page, size));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public Map<String, Object> updateProduct(
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID id,
            @RequestBody UpdateProductRequest request) {
        ProductResponse response = updateProductUseCase.execute(
            id, request.name(), request.description(), request.priceAmount(), request.priceCurrency(), request.stockQuantity()
        );
        return presenter.presentUpdateProduct(response);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public Map<String, Object> deleteProduct(@org.springframework.web.bind.annotation.PathVariable java.util.UUID id) {
        boolean success = deleteProductUseCase.execute(id);
        return presenter.presentDeleteProduct(success);
    }
}

record UpdateProductRequest(String name, String description, java.math.BigDecimal priceAmount, String priceCurrency, Integer stockQuantity) {}
