package com.ecommerce.order.adapter.in.presenter;

import com.ecommerce.order.usecase.PlaceOrderOutput;

import com.ecommerce.order.usecase.GetOrderByIdOutput;
import com.ecommerce.order.usecase.GetOrdersOutput;
import com.ecommerce.order.entity.Order;
import org.springframework.stereotype.Component; // removed

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Map<String, Object> presentGetOrder(GetOrderByIdOutput output) {
        Map<String, Object> viewModel = new LinkedHashMap<>();
        if (!output.success() || output.order() == null) {
            viewModel.put("error", output.message());
            return viewModel;
        }
        return presentSingleOrder(output.order());
    }

    public Map<String, Object> presentGetOrders(GetOrdersOutput output) {
        Map<String, Object> viewModel = new LinkedHashMap<>();
        if (!output.success()) {
            viewModel.put("error", output.message());
            return viewModel;
        }
        
        List<Map<String, Object>> ordersList = output.orders().stream()
                .map(this::presentSingleOrder)
                .collect(Collectors.toList());
        
        viewModel.put("orders", ordersList);
        return viewModel;
    }

    private Map<String, Object> presentSingleOrder(Order order) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", order.getId().toString());
        map.put("status", formatStatus(order.getStatus().name()));
        map.put("createdAt", order.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        
        if (order.getTotalAmount() != null) {
            map.put("totalAmount", order.getTotalAmount().getAmount().toPlainString());
            map.put("currency", order.getTotalAmount().getCurrency());
        }
        
        List<Map<String, Object>> items = order.getItems().stream().map(item -> {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("productId", item.getProductId().toString());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("price", item.getPrice().getAmount().toPlainString());
            return itemMap;
        }).collect(Collectors.toList());
        
        map.put("items", items);
        return map;
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
