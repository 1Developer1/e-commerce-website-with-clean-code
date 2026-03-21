package com.ecommerce.order.usecase;

import com.ecommerce.order.entity.Order;
import java.util.Optional;

public class GetOrderByIdUseCase {
    private final OrderRepository orderRepository;

    public GetOrderByIdUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public GetOrderByIdOutput execute(GetOrderByIdInput input) {
        Optional<Order> orderOpt = orderRepository.findById(input.orderId());
        
        if (orderOpt.isEmpty()) {
            return GetOrderByIdOutput.failure("Order completely absent");
        }
        
        Order order = orderOpt.get();
        if (!order.getUserId().equals(input.userId())) {
             return GetOrderByIdOutput.failure("Unauthorized access to order");
        }
        
        return GetOrderByIdOutput.success(order);
    }
}
