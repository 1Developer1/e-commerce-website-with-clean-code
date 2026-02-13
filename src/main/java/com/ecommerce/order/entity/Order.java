package com.ecommerce.order.entity;

import com.ecommerce.shared.domain.Money;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

public class Order {
    public enum Status {
        CREATED, PAID, SHIPPED, DELIVERED, CANCELLED
    }

    private final UUID id;
    private final UUID userId;
    private final List<OrderItem> items;
    private Status status;
    private final LocalDateTime createdAt;
    private final Money totalAmount;

    public Order(UUID id, UUID userId, List<OrderItem> items) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.status = Status.CREATED;
        this.createdAt = LocalDateTime.now();
        this.totalAmount = calculateTotal();
    }
    
    public static Order create(UUID userId, List<OrderItem> items) {
        return new Order(UUID.randomUUID(), userId, items);
    }
    
    private Money calculateTotal() {
        return items.stream()
                .map(OrderItem::getSubTotal)
                .reduce(new Money(BigDecimal.ZERO, "USD"), Money::add);
    }

    public void pay() {
        if (this.status != Status.CREATED) {
            throw new IllegalStateException("Order already paid or cancelled");
        }
        this.status = Status.PAID;
    }

    public UUID getId() {
        return id;
    }
    
    public UUID getUserId() {
        return userId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public Money getTotalAmount() {
        return totalAmount;
    }
}
