package com.ecommerce.order.usecase;

import com.ecommerce.cart.api.CartService;
import com.ecommerce.cart.usecase.dto.CartDto;
import com.ecommerce.cart.usecase.dto.CartItemDto;
import com.ecommerce.order.entity.Order;
import com.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlaceOrderUseCaseTest {

    private OrderRepository orderRepository;
    private CartService cartService;
    private PlaceOrderUseCase placeOrderUseCase;

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock(OrderRepository.class);
        cartService = Mockito.mock(CartService.class);
        placeOrderUseCase = new PlaceOrderUseCase(orderRepository, cartService);
    }

    @Test
    void shouldPlaceOrderSuccessfully() {
        UUID userId = UUID.randomUUID();
        
        CartItemDto itemDto = new CartItemDto(UUID.randomUUID(), 2, Money.of(BigDecimal.TEN, "USD"), Money.of(new BigDecimal("20.00"), "USD"));
        CartDto cartDto = new CartDto(userId, List.of(itemDto), Money.of(BigDecimal.ZERO, "USD"), Money.of(new BigDecimal("20.00"), "USD"));

        when(cartService.getCartForOrder(userId)).thenReturn(Optional.of(cartDto));

        PlaceOrderInput input = new PlaceOrderInput(userId);
        PlaceOrderOutput output = placeOrderUseCase.execute(input);

        Assertions.assertTrue(output.success());
        Assertions.assertNotNull(output.orderId());
        Assertions.assertEquals("CREATED", output.status());
        Assertions.assertEquals(new BigDecimal("20.00"), output.totalAmount());

        // Verify Cart is cleared
        verify(cartService, times(1)).clearCart(userId);
        
        // Verify Order is saved
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        Assertions.assertEquals(userId, savedOrder.getUserId());
        Assertions.assertEquals(1, savedOrder.getItems().size());
    }

    @Test
    void shouldFailIfCartEmpty() {
        UUID userId = UUID.randomUUID();
        CartDto emptyCart = new CartDto(userId, Collections.emptyList(), Money.of(BigDecimal.ZERO, "USD"), Money.of(BigDecimal.ZERO, "USD"));

        when(cartService.getCartForOrder(userId)).thenReturn(Optional.of(emptyCart));

        PlaceOrderInput input = new PlaceOrderInput(userId);
        PlaceOrderOutput output = placeOrderUseCase.execute(input);

        Assertions.assertFalse(output.success());
        Assertions.assertEquals("Cart is empty or not found", output.message());
        
        verify(orderRepository, never()).save(any());
        verify(cartService, never()).clearCart(any());
    }
}
