package com.ecommerce.cart.adapter.in.presenter;

import com.ecommerce.cart.usecase.AddToCartOutput;
import com.ecommerce.cart.usecase.ApplyDiscountOutput;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Presenter for Cart module.
 * Transforms UseCase ResponseModels into View-friendly ViewModels.
 */
@Component
public class CartPresenter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public Map<String, Object> presentAddToCart(AddToCartOutput output) {
        Map<String, Object> viewModel = new LinkedHashMap<>();
        viewModel.put("success", output.success());
        viewModel.put("message", output.message());
        viewModel.put("timestamp", LocalDateTime.now().format(FORMATTER));
        return viewModel;
    }

    public Map<String, Object> presentApplyDiscount(ApplyDiscountOutput output) {
        Map<String, Object> viewModel = new LinkedHashMap<>();
        viewModel.put("success", output.success());
        viewModel.put("message", output.message());
        viewModel.put("timestamp", LocalDateTime.now().format(FORMATTER));
        return viewModel;
    }

    public Map<String, Object> presentGetCart(com.ecommerce.cart.usecase.GetCartOutput output) {
        Map<String, Object> viewModel = new LinkedHashMap<>();
        if (!output.success() || output.cart() == null) {
            viewModel.put("error", output.message());
            return viewModel;
        }
        
        var cart = output.cart();
        viewModel.put("userId", cart.getUserId().toString());

        java.util.List<Map<String, Object>> items = cart.getItems().stream().map(item -> {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("productId", item.getProductId().toString());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("unitPrice", item.getUnitPrice().getAmount().toPlainString());
            itemMap.put("unitPriceCurrency", item.getUnitPrice().getCurrency());
            itemMap.put("itemTotal", item.getTotalPrice().getAmount().toPlainString());
            itemMap.put("itemTotalCurrency", item.getTotalPrice().getCurrency());
            return itemMap;
        }).collect(java.util.stream.Collectors.toList());
        viewModel.put("items", items);

        if (cart.getDiscount() != null) {
            viewModel.put("discountAmount", cart.getDiscount().getAmount().toPlainString());
            viewModel.put("discountCurrency", cart.getDiscount().getCurrency());
        }

        var total = cart.getTotalPrice();
        viewModel.put("totalPrice", total.getAmount().toPlainString());
        viewModel.put("totalCurrency", total.getCurrency());

        return viewModel;
    }
}
