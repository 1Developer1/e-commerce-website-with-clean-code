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

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
}
