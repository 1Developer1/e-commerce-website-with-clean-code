package com.ecommerce.cart.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import com.ecommerce.cart.entity.Cart;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetCartUseCaseTest {

    @Test
    @DisplayName("Sepet başarıyla getirilmeli")
    void shouldReturnCartSuccessfully() {
        // Arrange
        CartRepository cartRepository = mock(CartRepository.class);
        GetCartUseCase useCase = new GetCartUseCase(cartRepository);

        UUID userId = UUID.randomUUID();
        Cart cart = Cart.create(userId);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        GetCartInput input = new GetCartInput(userId);

        // Act
        GetCartOutput output = useCase.execute(input);

        // Assert
        assertTrue(output.success());
        assertNotNull(output.cart());
        assertEquals(userId, output.cart().getUserId());
        verify(cartRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Kullanıcının sepeti yoksa yeni boş sepet dönmeli (veya Not Found)")
    void shouldReturnEmptyCartWhenNotFound() {
        // Arrange
        CartRepository cartRepository = mock(CartRepository.class);
        GetCartUseCase useCase = new GetCartUseCase(cartRepository);

        UUID userId = UUID.randomUUID();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        GetCartInput input = new GetCartInput(userId);

        // Act
        GetCartOutput output = useCase.execute(input);

        // Assert
        assertTrue(output.success());
        assertNotNull(output.cart());
        assertEquals(userId, output.cart().getUserId());
        assertTrue(output.cart().getItems().isEmpty());
    }
}
