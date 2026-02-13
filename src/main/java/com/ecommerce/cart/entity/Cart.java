package com.ecommerce.cart.entity;

import com.ecommerce.product.entity.Product;
import com.ecommerce.shared.domain.Money;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Cart {
    private final UUID id;
    private final UUID userId;
    private final List<CartItem> items;

    public Cart(UUID id, UUID userId) {
        this.id = id;
        this.userId = userId;
        this.items = new ArrayList<>();
    }
    
    public static Cart create(UUID userId) {
        return new Cart(UUID.randomUUID(), userId);
    }

    public void addItem(Product product, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        
        Optional<CartItem> existingItem = items.stream()
            .filter(item -> item.getProductId().equals(product.getId()))
            .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().increaseQuantity(quantity);
        } else {
            items.add(new CartItem(product.getId(), quantity, product.getPrice()));
        }
    }

    public void removeItem(UUID productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
    }
    
    public void clear() {
        items.clear();
    }

    public Money getTotalPrice() {
        return items.stream()
            .map(CartItem::getSubTotal)
            .reduce(new Money(BigDecimal.ZERO, "USD"), Money::add); // Assuming USD for now, strictly should handle mixed currencies or enforce single currency
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
