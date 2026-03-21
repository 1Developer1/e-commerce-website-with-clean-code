package com.ecommerce.order.usecase;

import com.ecommerce.order.entity.Order;

public record GetOrderByIdOutput(boolean success, String message, Order order) {
    public static GetOrderByIdOutput success(Order order) {
        return new GetOrderByIdOutput(true, "Success", order);
    }

    public static GetOrderByIdOutput failure(String message) {
        return new GetOrderByIdOutput(false, message, null);
    }
}
