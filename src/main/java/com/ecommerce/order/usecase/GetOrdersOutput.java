package com.ecommerce.order.usecase;

import com.ecommerce.order.entity.Order;
import java.util.List;

public record GetOrdersOutput(boolean success, String message, List<Order> orders) {
    public static GetOrdersOutput success(List<Order> orders) {
        return new GetOrdersOutput(true, "Success", orders);
    }

    public static GetOrdersOutput failure(String message) {
        return new GetOrdersOutput(false, message, List.of());
    }
}
