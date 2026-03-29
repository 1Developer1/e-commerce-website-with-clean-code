package com.ecommerce.order.adapter.out.persistence.jpa;

import com.ecommerce.order.adapter.out.persistence.jpa.entity.OrderItemJpaEntity;
import com.ecommerce.order.adapter.out.persistence.jpa.entity.OrderJpaEntity;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.usecase.OrderRepository;
import com.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderSpringRepository orderSpringRepository;

    public OrderPersistenceAdapter(OrderSpringRepository orderSpringRepository) {
        this.orderSpringRepository = orderSpringRepository;
    }

    @Override
    public void save(Order order) {
        OrderJpaEntity entity = mapToJpaEntity(order);
        orderSpringRepository.save(entity);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderSpringRepository.findById(id).map(this::mapToDomainEntity);
    }

    @Override
    public List<Order> findByUserId(UUID userId, int page, int size) {
        return orderSpringRepository.findByUserId(userId, org.springframework.data.domain.PageRequest.of(page, size)).stream()
                .map(this::mapToDomainEntity)
                .collect(Collectors.toList());
    }

    // --- Mappers ---

    private OrderJpaEntity mapToJpaEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(order.getId());
        entity.setUserId(order.getUserId());
        entity.setRecipientName(order.getRecipientName());
        entity.setShippingAddress(order.getShippingAddress());
        entity.setStatus(order.getStatus().name());
        entity.setCreatedAt(order.getCreatedAt());

        if (order.getTotalAmount() != null) {
            entity.setTotalAmount(order.getTotalAmount().getAmount());
            entity.setCurrency(order.getTotalAmount().getCurrency());
        }

        if (order.getDiscount() != null) {
            entity.setDiscountAmount(order.getDiscount().getAmount());
            entity.setDiscountCurrency(order.getDiscount().getCurrency());
        } else {
            entity.setDiscountAmount(java.math.BigDecimal.ZERO);
            entity.setDiscountCurrency("USD");
        }

        List<OrderItemJpaEntity> itemEntities = order.getItems().stream().map(item -> {
            OrderItemJpaEntity itemEntity = new OrderItemJpaEntity();
            itemEntity.setProductId(item.getProductId());
            itemEntity.setQuantity(item.getQuantity());
            itemEntity.setPriceAmount(item.getPrice().getAmount());
            itemEntity.setPriceCurrency(item.getPrice().getCurrency());
            return itemEntity;
        }).collect(Collectors.toList());

        itemEntities.forEach(entity::addItem);

        return entity;
    }

    private Order mapToDomainEntity(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream().map(itemEntity ->
            new OrderItem(
                itemEntity.getProductId(),
                itemEntity.getQuantity(),
                Money.of(itemEntity.getPriceAmount(), itemEntity.getPriceCurrency())
            )
        ).collect(Collectors.toList());

        Money discount = Money.of(entity.getDiscountAmount(), entity.getDiscountCurrency());
        Money totalAmount = Money.of(entity.getTotalAmount(), entity.getCurrency());
        Order.Status status = Order.Status.valueOf(entity.getStatus());

        return Order.restore(
            entity.getId(),
            entity.getUserId(),
            entity.getRecipientName(),
            entity.getShippingAddress(),
            items,
            discount,
            status,
            entity.getCreatedAt(),
            totalAmount
        );
    }
}
