package com.ecommerce.product.adapter.out.persistence.jpa;

import com.ecommerce.product.adapter.out.persistence.jpa.entity.ProductJpaEntity;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.usecase.ProductRepository;
import com.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;

@Component
public class ProductPersistenceAdapter implements ProductRepository {

    private final ProductSpringRepository productSpringRepository;

    public ProductPersistenceAdapter(ProductSpringRepository productSpringRepository) {
        this.productSpringRepository = productSpringRepository;
    }

    @Override
    public void save(Product product) {
        ProductJpaEntity entity = mapToJpaEntity(product);
        productSpringRepository.save(entity);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return productSpringRepository.findById(id).map(this::mapToDomainEntity);
    }

    @Override
    public List<Product> findAll() {
        return productSpringRepository.findAll().stream()
                .map(this::mapToDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findAll(int page, int size) {
        return productSpringRepository.findAll(PageRequest.of(page, size)).stream()
                .map(this::mapToDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return productSpringRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        productSpringRepository.deleteById(id);
    }

    // --- Mappers ---

    private ProductJpaEntity mapToJpaEntity(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId(product.getId());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setPriceAmount(product.getPrice().getAmount());
        entity.setPriceCurrency(product.getPrice().getCurrency());
        entity.setStockQuantity(product.getStockQuantity());
        return entity;
    }

    private Product mapToDomainEntity(ProductJpaEntity entity) {
        Money price = Money.of(entity.getPriceAmount(), entity.getPriceCurrency());
        return new Product(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                price,
                entity.getStockQuantity()
        );
    }
}
