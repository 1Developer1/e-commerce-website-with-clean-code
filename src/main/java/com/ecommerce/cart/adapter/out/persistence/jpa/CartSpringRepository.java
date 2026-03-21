package com.ecommerce.cart.adapter.out.persistence.jpa;

import com.ecommerce.cart.adapter.out.persistence.jpa.entity.CartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CartSpringRepository extends JpaRepository<CartJpaEntity, UUID> {
    Optional<CartJpaEntity> findByUserId(UUID userId);
}
