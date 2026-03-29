package com.ecommerce.user.entity;

import java.util.UUID;

public class User {
    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final String name;

    public User(UUID id, String email, String passwordHash, String name) {
        if (id == null) throw new IllegalArgumentException("User ID cannot be null");
        if (email == null || email.trim().isEmpty()) throw new IllegalArgumentException("Email cannot be empty");
        if (passwordHash == null || passwordHash.trim().isEmpty()) throw new IllegalArgumentException("Password hash cannot be empty");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be empty");

        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
    }

    public static User create(String email, String passwordHash, String name) {
        return new User(UUID.randomUUID(), email, passwordHash, name);
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getName() { return name; }
}
