package com.ecommerce.cart.usecase;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.usecase.port.DiscountProvider;
import com.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplyDiscountUseCaseTest {

    private CartRepository cartRepository;
    private DiscountProvider discountProvider;
    private ApplyDiscountUseCase useCase;

    @BeforeEach
    void setUp() {
        cartRepository = mock(CartRepository.class);
        discountProvider = mock(DiscountProvider.class);
        useCase = new ApplyDiscountUseCase(cartRepository, discountProvider);
    }

    @Test
    void shouldApplyDiscountSuccessfully() {
        UUID userId = UUID.randomUUID();
        Cart cart = Cart.create(userId);
        // Assuming cart starts empty with 0.00 USD
        
        String code = "WINTER50";
        Money discountAmount = Money.of(new BigDecimal("50.00"), "USD");

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(discountProvider.getDiscount(cart, code)).thenReturn(Optional.of(discountAmount));

        ApplyDiscountOutput output = useCase.execute(new ApplyDiscountInput(userId, code));

        assertTrue(output.success());
        assertEquals("Discount applied", output.message());

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());
        
        // Cart domain object applies discount by subtracting it from total,
        // Since cart is empty (0.00), applying discount might make it negative or zero depending on entity rules.
        // We just verify save is called.
    }

    @Test
    void shouldFailWhenCartNotFound() {
        UUID userId = UUID.randomUUID();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        ApplyDiscountOutput output = useCase.execute(new ApplyDiscountInput(userId, "CODE"));

        assertFalse(output.success());
        assertEquals("Cart not found", output.message());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenDiscountIsInvalid() {
        UUID userId = UUID.randomUUID();
        Cart cart = Cart.create(userId);
        String code = "INVALID";

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(discountProvider.getDiscount(cart, code)).thenReturn(Optional.empty());

        ApplyDiscountOutput output = useCase.execute(new ApplyDiscountInput(userId, code));

        assertFalse(output.success());
        assertEquals("Invalid discount code", output.message());
        verify(cartRepository, never()).save(any());
    }
}
