package com.ecommerce.product.entity;

import com.ecommerce.shared.domain.Money;
import java.util.UUID;

public class Product {
    private final UUID id;
    private final String name;
    private final String description;
    private final Money price;
    private int stockQuantity;

    public Product(UUID id, String name, String description, Money price, int stockQuantity) {
        validate(id, name, price, stockQuantity);

        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    private void validate(UUID id, String name, Money price, int stockQuantity) {
        if (id == null) throw new IllegalArgumentException("Product ID cannot be null");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Product name cannot be empty");
        if (price == null) throw new IllegalArgumentException("Product price cannot be null");
        if (stockQuantity < 0) throw new IllegalArgumentException("Stock quantity cannot be negative");
    }

    public static Product create(String name, String description, Money price, int initialStock) {
        return new Product(UUID.randomUUID(), name, description, price, initialStock);
    }

    // Business Methods

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to decrease must be positive");
        }
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
         if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to increase must be positive");
        }
        this.stockQuantity += quantity;
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Money getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }
}
