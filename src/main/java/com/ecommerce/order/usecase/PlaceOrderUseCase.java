package com.ecommerce.order.usecase;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.usecase.CartRepository;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class PlaceOrderUseCase {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    public PlaceOrderUseCase(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    public PlaceOrderOutput execute(PlaceOrderInput input) {
        return cartRepository.findByUserId(input.userId())
                .filter(cart -> !cart.isEmpty())
                .map(this::createOrderFromCart)
                .orElseGet(() -> new PlaceOrderOutput(false, "Cart is empty or not found", null, null, null));
    }

    private PlaceOrderOutput createOrderFromCart(Cart cart) {
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(item -> new OrderItem(item.getProductId(), item.getQuantity(), item.getPrice()))
                .collect(Collectors.toList());

        Order order = Order.create(cart.getUserId(), orderItems);
        orderRepository.save(order);
        
        cart.clear();
        cartRepository.save(cart);

        return new PlaceOrderOutput(
            true, 
            "Order placed successfully", 
            order.getId(), 
            order.getStatus().name(), 
            order.getTotalAmount().getAmount()
        );
    }
}
