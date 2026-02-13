package com.ecommerce.order.usecase;

import com.ecommerce.cart.usecase.CartService;
import com.ecommerce.cart.usecase.dto.CartDto;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class PlaceOrderUseCase {
    private final OrderRepository orderRepository;
    private final CartService cartService;

    public PlaceOrderUseCase(OrderRepository orderRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    public PlaceOrderOutput execute(PlaceOrderInput input) {
        // Using Facade Service to get DTO
        return cartService.getCartForOrder(input.userId())
                .filter(cart -> !cart.isEmpty())
                .map(this::createOrderFromCart)
                .orElseGet(() -> new PlaceOrderOutput(false, "Cart is empty or not found", null, null, null));
    }

    private PlaceOrderOutput createOrderFromCart(CartDto cart) {
        List<OrderItem> orderItems = cart.items().stream()
                .map(item -> new OrderItem(item.productId(), item.quantity(), item.unitPrice()))
                .collect(Collectors.toList());
        
        Order order = Order.create(cart.userId(), orderItems, cart.discount());
        orderRepository.save(order);
        
        // Clearing cart via Facade
        cartService.clearCart(cart.userId());

        return new PlaceOrderOutput(
            true, 
            "Order placed successfully", 
            order.getId(), 
            order.getStatus().name(), 
            order.getTotalAmount().getAmount()
        );
    }
}
