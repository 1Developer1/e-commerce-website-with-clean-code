package com.ecommerce.order.adapter.in.presenter;

import com.ecommerce.order.usecase.PlaceOrderOutput;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Presenter for Order module.
 * Transforms UseCase ResponseModels into View-friendly ViewModels.
 * Clean Architecture: This class lives in the Interface Adapters layer.
 */
public class OrderPresenter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Transforms PlaceOrderOutput into a View-friendly Map.
     * Formats monetary values and adds user-facing labels.
     */
    public Map<String, Object> presentPlaceOrder(PlaceOrderOutput output) {
        Map<String, Object> viewModel = new LinkedHashMap<>();
        viewModel.put("success", output.success());
        viewModel.put("message", output.message());

        if (output.success() && output.orderId() != null) {
            viewModel.put("orderId", output.orderId().toString());
            viewModel.put("orderStatus", formatStatus(output.status()));
            viewModel.put("displayTotal", "USD " + output.totalAmount().toPlainString());
        }

        viewModel.put("timestamp", LocalDateTime.now().format(FORMATTER));
        return viewModel;
    }

    /**
     * User-friendly status label.
     */
    private String formatStatus(String rawStatus) {
        if (rawStatus == null) return "Unknown";
        return switch (rawStatus) {
            case "CREATED" -> "Sipariş Oluşturuldu";
            case "PAID" -> "Ödeme Tamamlandı";
            case "SHIPPED" -> "Kargoya Verildi";
            case "DELIVERED" -> "Teslim Edildi";
            case "CANCELLED" -> "İptal Edildi";
            default -> rawStatus;
        };
    }
}
