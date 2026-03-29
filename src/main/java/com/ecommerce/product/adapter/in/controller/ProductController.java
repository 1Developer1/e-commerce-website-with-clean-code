package com.ecommerce.product.adapter.in.controller;

import com.ecommerce.product.usecase.CreateProductInput;
import com.ecommerce.product.usecase.CreateProductUseCase;
import com.ecommerce.product.usecase.ListProductsUseCase;
import com.ecommerce.product.usecase.UpdateProductUseCase;
import com.ecommerce.product.usecase.DeleteProductUseCase;
import com.ecommerce.product.usecase.dto.ProductResponse;
import com.ecommerce.product.adapter.in.presenter.ProductPresenter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Products", description = "Ürün kataloğu CRUD işlemleri")
@RestController
@RequestMapping("/api/v1/products")
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

    @Operation(summary = "Yeni ürün oluşturur", description = "Validated input ile ürün oluşturur. Başarılı: 201 Created.")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        CreateProductInput input = new CreateProductInput(
                request.name(), request.description(),
                request.priceAmount(), request.priceCurrency(),
                request.initialStock()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(presenter.presentCreateProduct(createProductUseCase.execute(input)));
    }

    @Operation(summary = "Ürünleri listeler", description = "Sayfalanmış ürün listesi döner (max 100/sayfa).")
    @GetMapping
    public ResponseEntity<Map<String, Object>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var output = listProductsUseCase.execute(page, size);
        int totalPages = size > 0 ? (int) Math.ceil((double) output.totalElements() / size) : 0;
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(output.totalElements()))
                .header("X-Total-Pages", String.valueOf(totalPages))
                .body(presenter.presentProductList(output));
    }

    @Operation(summary = "Ürünü günceller", description = "Mevcut ürünü kısmi olarak günceller.")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse response = updateProductUseCase.execute(
            id, request.name(), request.description(), request.priceAmount(), request.priceCurrency(), request.stockQuantity()
        );
        return ResponseEntity.ok(presenter.presentUpdateProduct(response));
    }

    @Operation(summary = "Ürünü siler", description = "Ürünü ID ile siler. Başarılı: 204 No Content. Bulunamazsa: 404 Not Found.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        boolean deleted = deleteProductUseCase.execute(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
