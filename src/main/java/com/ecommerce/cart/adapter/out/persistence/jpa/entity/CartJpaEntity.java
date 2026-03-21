package com.ecommerce.cart.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
public class CartJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    private BigDecimal discountAmount;
    private String discountCurrency;

    @ElementCollection
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
    private List<CartItemEmbeddable> items = new ArrayList<>();

    public CartJpaEntity() {
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getDiscountCurrency() {
        return discountCurrency;
    }
    public void setDiscountCurrency(String discountCurrency) {
        this.discountCurrency = discountCurrency;
    }

    public List<CartItemEmbeddable> getItems() {
        return items;
    }
    public void setItems(List<CartItemEmbeddable> items) {
        this.items = items;
    }
}
