package com.ecommerce.cart.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Embeddable
public class CartItemEmbeddable {

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal priceAmount;

    @Column(nullable = false)
    private String priceCurrency;

    public CartItemEmbeddable() {
    }

    public CartItemEmbeddable(UUID productId, int quantity, BigDecimal priceAmount, String priceCurrency) {
        this.productId = productId;
        this.quantity = quantity;
        this.priceAmount = priceAmount;
        this.priceCurrency = priceCurrency;
    }

    public UUID getProductId() {
        return productId;
    }
    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAmount() {
        return priceAmount;
    }
    public void setPriceAmount(BigDecimal priceAmount) {
        this.priceAmount = priceAmount;
    }

    public String getPriceCurrency() {
        return priceCurrency;
    }
    public void setPriceCurrency(String priceCurrency) {
        this.priceCurrency = priceCurrency;
    }
}
