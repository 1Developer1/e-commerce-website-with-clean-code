package com.ecommerce.cart.adapter.out.persistence.jpa;

import com.ecommerce.cart.adapter.out.persistence.jpa.entity.CartItemEmbeddable;
import com.ecommerce.cart.adapter.out.persistence.jpa.entity.CartJpaEntity;
import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.usecase.CartRepository;
import com.ecommerce.cart.usecase.dto.CartDto;
import com.ecommerce.cart.usecase.dto.CartItemDto;
import com.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CartPersistenceAdapter implements CartRepository {

    private final CartSpringRepository cartSpringRepository;

    public CartPersistenceAdapter(CartSpringRepository cartSpringRepository) {
        this.cartSpringRepository = cartSpringRepository;
    }

    @Override
    public void save(Cart cart) {
        CartJpaEntity entity = mapToJpaEntity(cart);
        cartSpringRepository.save(entity);
    }

    @Override
    public Optional<Cart> findByUserId(UUID userId) {
        return cartSpringRepository.findByUserId(userId)
                .map(this::mapToDomainEntity);
    }

    @Override
    public Optional<CartDto> findDtoByUserId(UUID userId) {
        return cartSpringRepository.findByUserId(userId)
                .map(this::mapToDomainEntity)
                .map(this::mapToDto);
    }

    // --- Mappers ---

    private CartJpaEntity mapToJpaEntity(Cart cart) {
        CartJpaEntity entity = new CartJpaEntity();
        entity.setId(cart.getId());
        entity.setUserId(cart.getUserId());
        
        if (cart.getDiscount() != null) {
            entity.setDiscountAmount(cart.getDiscount().getAmount());
            entity.setDiscountCurrency(cart.getDiscount().getCurrency());
        }

        List<CartItemEmbeddable> embeddableItems = cart.getItems().stream()
                .map(item -> new CartItemEmbeddable(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice().getAmount(),
                        item.getPrice().getCurrency()
                ))
                .collect(Collectors.toList());
        entity.setItems(embeddableItems);

        return entity;
    }

    private Cart mapToDomainEntity(CartJpaEntity entity) {
        List<CartItem> items = entity.getItems().stream()
                .map(embeddable -> new CartItem(
                        embeddable.getProductId(),
                        embeddable.getProductName(),
                        embeddable.getQuantity(),
                        Money.of(embeddable.getPriceAmount(), embeddable.getPriceCurrency())
                ))
                .collect(Collectors.toList());

        Money discount = Money.of(java.math.BigDecimal.ZERO, "USD");
        if (entity.getDiscountAmount() != null && entity.getDiscountCurrency() != null) {
            discount = Money.of(entity.getDiscountAmount(), entity.getDiscountCurrency());
        }

        return Cart.restore(entity.getId(), entity.getUserId(), items, discount);
    }

    private CartDto mapToDto(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream()
                .map(item -> new CartItemDto(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .collect(Collectors.toList());

        return new CartDto(cart.getUserId(), items, cart.getDiscount(), cart.getTotalPrice());
    }
}
