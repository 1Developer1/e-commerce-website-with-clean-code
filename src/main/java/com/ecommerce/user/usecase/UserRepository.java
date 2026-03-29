package com.ecommerce.user.usecase;

import com.ecommerce.user.entity.User;
import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
