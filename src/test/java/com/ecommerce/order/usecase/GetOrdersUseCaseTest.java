package com.ecommerce.order.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import com.ecommerce.order.entity.Order;
import com.ecommerce.shared.domain.Money;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetOrdersUseCaseTest {

    @Test
    @DisplayName("Kullanıcının siparişlerini sayfa sayfa getir")
    void shouldReturnOrdersWithPagination() {
        // Arrange
        OrderRepository repository = mock(OrderRepository.class);
        GetOrdersUseCase useCase = new GetOrdersUseCase(repository);

        UUID userId = UUID.randomUUID();
        Order order1 = Order.restore(UUID.randomUUID(), userId, java.util.Collections.emptyList(), null, Order.Status.CREATED, java.time.LocalDateTime.now(), Money.of(new java.math.BigDecimal("100"), "USD"));
        Order order2 = Order.restore(UUID.randomUUID(), userId, java.util.Collections.emptyList(), null, Order.Status.CREATED, java.time.LocalDateTime.now(), Money.of(new java.math.BigDecimal("200"), "USD"));
        
        when(repository.findByUserId(userId, 0, 10)).thenReturn(List.of(order1, order2));

        GetOrdersInput input = new GetOrdersInput(userId, 0, 10);

        // Act
        GetOrdersOutput output = useCase.execute(input);

        // Assert
        assertTrue(output.success());
        assertEquals(2, output.orders().size());
        verify(repository, times(1)).findByUserId(userId, 0, 10);
    }
}
