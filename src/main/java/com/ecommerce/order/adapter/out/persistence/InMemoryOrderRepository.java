package com.ecommerce.order.adapter.out.persistence;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.usecase.OrderRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryOrderRepository implements OrderRepository {
    private final Map<UUID, Order> orders = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        orders.put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<Order> findByUserId(UUID userId) {
        return orders.values().stream()
                .filter(order -> order.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}
