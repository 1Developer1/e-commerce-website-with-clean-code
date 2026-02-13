package com.ecommerce.product.usecase;

import java.util.UUID;

public record CreateProductOutput(UUID id, String name, boolean success, String message) {
}
