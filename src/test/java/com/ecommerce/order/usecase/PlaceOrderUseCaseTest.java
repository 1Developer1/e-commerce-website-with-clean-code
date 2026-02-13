package com.ecommerce.order.usecase;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.usecase.CartRepository;
import com.ecommerce.order.entity.Order;
import com.ecommerce.product.entity.Product;
import com.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlaceOrderUseCaseTest {

    private OrderRepository orderRepository;
    private CartRepository cartRepository;
    private PlaceOrderUseCase placeOrderUseCase;

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock(OrderRepository.class);
        cartRepository = Mockito.mock(CartRepository.class);
        placeOrderUseCase = new PlaceOrderUseCase(orderRepository, cartRepository);
    }

    @Test
    void shouldPlaceOrderSuccessfully() {
        UUID userId = UUID.randomUUID();
        Cart cart = new Cart(UUID.randomUUID(), userId);
        Product product = new Product(UUID.randomUUID(), "P1", "D1", Money.of(BigDecimal.TEN, "USD"), 10);
        cart.addItem(product, 2); // Total 20

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        PlaceOrderInput input = new PlaceOrderInput(userId);
        PlaceOrderOutput output = placeOrderUseCase.execute(input);

        Assertions.assertTrue(output.success());
        Assertions.assertNotNull(output.orderId());
        Assertions.assertEquals("CREATED", output.status());
        Assertions.assertEquals(new BigDecimal("20"), output.totalAmount());

        // Verify Cart is cleared
        Assertions.assertTrue(cart.getItems().isEmpty());
        verify(cartRepository, times(1)).save(cart);
        
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
        Cart cart = new Cart(UUID.randomUUID(), userId); // Empty

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        PlaceOrderInput input = new PlaceOrderInput(userId);
        PlaceOrderOutput output = placeOrderUseCase.execute(input);

        Assertions.assertFalse(output.success());
        Assertions.assertEquals("Cart is empty or not found", output.message());
        
        verify(orderRepository, never()).save(any());
    }
}
