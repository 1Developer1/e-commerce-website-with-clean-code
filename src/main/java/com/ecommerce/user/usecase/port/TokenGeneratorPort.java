package com.ecommerce.user.usecase.port;

import java.util.UUID;

public interface TokenGeneratorPort {
    String generateToken(UUID userId);
}
