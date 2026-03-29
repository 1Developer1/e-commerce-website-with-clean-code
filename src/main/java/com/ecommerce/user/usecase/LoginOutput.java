package com.ecommerce.user.usecase;

public record LoginOutput(boolean success, String message, String token, String userId) {}
