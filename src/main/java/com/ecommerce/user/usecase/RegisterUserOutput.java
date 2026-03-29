package com.ecommerce.user.usecase;

public record RegisterUserOutput(boolean success, String message, String userId, String email) {}
