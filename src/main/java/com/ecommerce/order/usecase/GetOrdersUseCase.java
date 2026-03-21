package com.ecommerce.order.usecase;

import com.ecommerce.order.entity.Order;
import java.util.List;

public class GetOrdersUseCase {
    private final OrderRepository orderRepository;

    public GetOrdersUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public GetOrdersOutput execute(GetOrdersInput input) {
        List<Order> orders = orderRepository.findByUserId(input.userId(), input.page(), input.size());
        return GetOrdersOutput.success(orders);
    }
}
