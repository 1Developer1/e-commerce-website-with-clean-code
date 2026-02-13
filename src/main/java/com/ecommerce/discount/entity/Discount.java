package com.ecommerce.discount.entity;

import com.ecommerce.shared.domain.Money;
import java.util.UUID;

public class Discount {
    private final UUID id;
    private final String code;
    private final Money amount;

    public Discount(UUID id, String code, Money amount) {
        if (id == null) throw new IllegalArgumentException("Discount ID cannot be null");
        if (code == null || code.trim().isEmpty()) throw new IllegalArgumentException("Discount code cannot be empty");
        if (amount == null) throw new IllegalArgumentException("Discount amount cannot be null");

        this.id = id;
        this.code = code;
        this.amount = amount;
    }

    public static Discount create(String code, Money amount) {
        return new Discount(UUID.randomUUID(), code, amount);
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Money getAmount() {
        return amount;
    }
}
