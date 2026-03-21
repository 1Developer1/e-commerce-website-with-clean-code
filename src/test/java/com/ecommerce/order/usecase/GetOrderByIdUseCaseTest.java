package com.ecommerce.order.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ecommerce.order.entity.Order;
import com.ecommerce.shared.domain.Money;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GetOrderByIdUseCase.
 */
class GetOrderByIdUseCaseTest {

    @Test
    @DisplayName("Siparişi başarıyla getir")
    void shouldReturnOrderSuccessfully() {
        // Arrange
        OrderRepository repository = mock(OrderRepository.class);
        GetOrderByIdUseCase useCase = new GetOrderByIdUseCase(repository);

        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Order order = Order.restore(orderId, userId, java.util.Collections.emptyList(), null, Order.Status.CREATED, java.time.LocalDateTime.now(), Money.of(new java.math.BigDecimal("100.00"), "USD"));

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        GetOrderByIdInput input = new GetOrderByIdInput(userId, orderId);

        // Act
        GetOrderByIdOutput output = useCase.execute(input);

        // Assert
        assertTrue(output.success());
        assertNotNull(output.order());
        assertEquals(orderId, output.order().getId());
        verify(repository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("Sipariş bulunamadığında hata fırlatmalı (veya başarısız dönmeli)")
    void shouldReturnErrorWhenOrderNotFound() {
        // Arrange
        OrderRepository repository = mock(OrderRepository.class);
        GetOrderByIdUseCase useCase = new GetOrderByIdUseCase(repository);

        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(repository.findById(orderId)).thenReturn(Optional.empty());

        GetOrderByIdInput input = new GetOrderByIdInput(userId, orderId);

        // Act
        GetOrderByIdOutput output = useCase.execute(input);

        // Assert
        assertFalse(output.success());
        assertEquals("Order completely absent", output.message());
        assertNull(output.order());
    }

    @Test
    @DisplayName("Kullanıcı başkasının siparişine erişmeye çalıştığında başarısız dönmeli")
    void shouldReturnErrorWhenUserTriesToAccessAnotherUsersOrder() {
        // Arrange
        OrderRepository repository = mock(OrderRepository.class);
        GetOrderByIdUseCase useCase = new GetOrderByIdUseCase(repository);

        UUID orderId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        Order order = Order.restore(orderId, ownerId, java.util.Collections.emptyList(), null, Order.Status.CREATED, java.time.LocalDateTime.now(), Money.of(new java.math.BigDecimal("100.00"), "USD"));

        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        GetOrderByIdInput input = new GetOrderByIdInput(attackerId, orderId);

        // Act
        GetOrderByIdOutput output = useCase.execute(input);

        // Assert
        assertFalse(output.success());
        assertEquals("Unauthorized access to order", output.message());
        assertNull(output.order());
    }
}
