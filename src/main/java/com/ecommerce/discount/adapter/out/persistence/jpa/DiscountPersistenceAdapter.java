package com.ecommerce.discount.adapter.out.persistence.jpa;

import com.ecommerce.discount.adapter.out.persistence.jpa.entity.DiscountJpaEntity;
import com.ecommerce.discount.entity.Discount;
import com.ecommerce.discount.usecase.DiscountRepository;
import com.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Optional;

@Component
public class DiscountPersistenceAdapter implements DiscountRepository {

    private final DiscountSpringRepository discountSpringRepository;

    public DiscountPersistenceAdapter(DiscountSpringRepository discountSpringRepository) {
        this.discountSpringRepository = discountSpringRepository;
    }

    @PostConstruct
    public void init() {
        if (discountSpringRepository.findByCode("SUMMER10").isEmpty()) {
            Discount sum = Discount.create("SUMMER10", Money.of(new BigDecimal("10.00"), "USD"));
            save(sum);
        }
    }

    @Override
    public void save(Discount discount) {
        DiscountJpaEntity entity = mapToJpaEntity(discount);
        discountSpringRepository.save(entity);
    }

    @Override
    public Optional<Discount> findByCode(String code) {
        return discountSpringRepository.findByCode(code).map(this::mapToDomainEntity);
    }

    // --- Mappers ---
    private DiscountJpaEntity mapToJpaEntity(Discount discount) {
        DiscountJpaEntity entity = new DiscountJpaEntity();
        entity.setId(discount.getId());
        entity.setCode(discount.getCode());
        entity.setAmount(discount.getAmount().getAmount());
        entity.setCurrency(discount.getAmount().getCurrency());
        return entity;
    }

    private Discount mapToDomainEntity(DiscountJpaEntity entity) {
        return new Discount(
                entity.getId(),
                entity.getCode(),
                Money.of(entity.getAmount(), entity.getCurrency())
        );
    }
}
