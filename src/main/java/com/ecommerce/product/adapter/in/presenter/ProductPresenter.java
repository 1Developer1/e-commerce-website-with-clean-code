package com.ecommerce.product.adapter.in.presenter;

import com.ecommerce.product.usecase.CreateProductOutput;
import com.ecommerce.product.usecase.ListProductsOutput;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Presenter for Product module.
 * Transforms UseCase ResponseModels into View-friendly ViewModels.
 * Clean Architecture: This class lives in the Interface Adapters layer.
 */
public class ProductPresenter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Transforms CreateProductOutput into a View-friendly Map.
     */
    public Map<String, Object> presentCreateProduct(CreateProductOutput output) {
        Map<String, Object> viewModel = new LinkedHashMap<>();
        viewModel.put("success", output.success());
        viewModel.put("message", output.message());
        if (output.success()) {
            viewModel.put("productId", output.id().toString());
            viewModel.put("productName", output.name());
        }
        viewModel.put("timestamp", LocalDateTime.now().format(FORMATTER));
        return viewModel;
    }

    /**
     * Transforms ListProductsOutput into a View-friendly Map with formatted prices.
     */
    public Map<String, Object> presentProductList(ListProductsOutput output) {
        List<Map<String, Object>> formattedProducts = output.products().stream()
                .map(p -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", p.id().toString());
                    item.put("name", p.name());
                    item.put("displayPrice", p.currency() + " " + p.price().toPlainString());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> viewModel = new LinkedHashMap<>();
        viewModel.put("page", output.page());
        viewModel.put("size", output.size());
        viewModel.put("totalElements", output.totalElements());
        viewModel.put("totalCount", formattedProducts.size());
        viewModel.put("products", formattedProducts);
        return viewModel;
    }

    public Map<String, Object> presentUpdateProduct(com.ecommerce.product.usecase.ProductResponse productResponse) {
        Map<String, Object> viewModel = new LinkedHashMap<>();
        viewModel.put("success", true);
        viewModel.put("message", "Product updated successfully");
        
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", productResponse.id());
        item.put("name", productResponse.name());
        item.put("displayPrice", productResponse.displayPrice());
        viewModel.put("product", item);
        return viewModel;
    }

    public Map<String, Object> presentDeleteProduct(boolean success) {
        Map<String, Object> viewModel = new LinkedHashMap<>();
        viewModel.put("success", success);
        viewModel.put("message", success ? "Product deleted successfully" : "Product not found or delete failed");
        return viewModel;
    }
}
