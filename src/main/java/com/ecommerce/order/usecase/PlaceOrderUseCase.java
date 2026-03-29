package com.ecommerce.order.usecase;

import com.ecommerce.cart.api.CartService;
import com.ecommerce.cart.usecase.dto.CartDto;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ecommerce.shared.event.EventBus;

public class PlaceOrderUseCase {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final EventBus eventBus;

    public PlaceOrderUseCase(OrderRepository orderRepository, CartService cartService, EventBus eventBus) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.eventBus = eventBus;
    }

    public PlaceOrderOutput execute(PlaceOrderInput input) {
        return cartService.getCartForOrder(input.userId())
                .filter(cart -> !cart.isEmpty())
                .map(cart -> createOrderFromCart(cart, input.recipientName(), input.shippingAddress()))
                .orElseGet(() -> new PlaceOrderOutput(false, "Cart is empty or not found", null, null, null));
    }

    private PlaceOrderOutput createOrderFromCart(CartDto cart, String recipientName, String shippingAddress) {
        List<OrderItem> orderItems = cart.items().stream()
                .map(item -> new OrderItem(item.productId(), item.quantity(), item.unitPrice()))
                .collect(Collectors.toList());

        Order order = Order.create(cart.userId(), recipientName, shippingAddress, orderItems, cart.discount());
        orderRepository.save(order);
        
        // Clearing cart via Facade
        cartService.clearCart(cart.userId());

        // Publish event for stock reduction
        Map<UUID, Integer> productQuantities = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity));
        eventBus.publish(new com.ecommerce.order.usecase.event.OrderPlacedEvent(order.getId(), productQuantities, java.time.LocalDateTime.now()));

        return new PlaceOrderOutput(
            true, 
            "Order placed successfully", 
            order.getId(), 
            order.getStatus().name(), 
            order.getTotalAmount().getAmount()
        );
    }
}
