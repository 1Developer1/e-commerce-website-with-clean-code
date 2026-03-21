package com.ecommerce.discount.adapter.out.persistence.jpa;

import com.ecommerce.discount.adapter.out.persistence.jpa.entity.DiscountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface DiscountSpringRepository extends JpaRepository<DiscountJpaEntity, UUID> {
    Optional<DiscountJpaEntity> findByCode(String code);
}
