package com.ecommerce.cart.entity;

import com.ecommerce.shared.domain.Money;
import java.util.UUID;

public class CartItem {
    private final UUID productId;
    private int quantity;
    private final Money price; // Snapshot price or current? For Cart often current but let's store it.

    public CartItem(UUID productId, int quantity, Money price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }

    public Money getSubTotal() {
        return price.multiply(quantity);
    }

    public UUID getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
    
    public Money getPrice() {
        return price;
    }

    // Alias for Mapper
    public Money getUnitPrice() {
        return price;
    }

    public Money getTotalPrice() {
        return getSubTotal();
    }
}
