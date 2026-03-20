package com.ecommerce.discount.usecase;

import com.ecommerce.discount.entity.Discount;
import com.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetDiscountUseCaseTest {

    private DiscountRepository discountRepository;
    private GetDiscountUseCase useCase;

    @BeforeEach
    void setUp() {
        discountRepository = mock(DiscountRepository.class);
        useCase = new GetDiscountUseCase(discountRepository);
    }

    @Test
    void shouldReturnDiscountWhenCodeIsValid() {
        String code = "SUMMER20";
        Money discountAmount = Money.of(new BigDecimal("20.00"), "USD");
        Discount mockDiscount = Discount.create(code, discountAmount);

        when(discountRepository.findByCode(code)).thenReturn(Optional.of(mockDiscount));

        GetDiscountOutput output = useCase.execute(new GetDiscountInput(code));

        assertTrue(output.isValid());
        assertEquals("Discount applied", output.message());
        assertEquals(discountAmount, output.amount());
    }

    @Test
    void shouldReturnErrorWhenCodeIsInvalid() {
        String code = "INVALID10";
        when(discountRepository.findByCode(code)).thenReturn(Optional.empty());

        GetDiscountOutput output = useCase.execute(new GetDiscountInput(code));

        assertFalse(output.isValid());
        assertEquals("Invalid discount code", output.message());
        assertNull(output.amount());
    }
}
