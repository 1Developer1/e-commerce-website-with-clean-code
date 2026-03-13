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
    private final Money discount; // New field
    private Status status;
    private final LocalDateTime createdAt;
    private final Money totalAmount;

    public Order(UUID id, UUID userId, List<OrderItem> items, Money discount) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.discount = discount != null ? discount : new Money(BigDecimal.ZERO, "USD");
        this.status = Status.CREATED;
        this.createdAt = LocalDateTime.now();
        this.totalAmount = calculateTotal();
    }
    
    private Order(UUID id, UUID userId, List<OrderItem> items, Money discount, Status status, LocalDateTime createdAt, Money totalAmount) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.discount = discount != null ? discount : new Money(BigDecimal.ZERO, "USD");
        this.status = status;
        this.createdAt = createdAt;
        this.totalAmount = totalAmount;
    }

    public static Order create(UUID userId, List<OrderItem> items, Money discount) {
        return new Order(UUID.randomUUID(), userId, items, discount);
    }
    
    public static Order restore(UUID id, UUID userId, List<OrderItem> items, Money discount, Status status, LocalDateTime createdAt, Money totalAmount) {
        return new Order(id, userId, items, discount, status, createdAt, totalAmount);
    }
    
    private Money calculateTotal() {
        Money subTotal = items.stream()
                .map(OrderItem::getSubTotal)
                .reduce(new Money(BigDecimal.ZERO, "USD"), Money::add);
        return subTotal.subtract(discount);
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

    public Money getDiscount() {
        return discount;
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
