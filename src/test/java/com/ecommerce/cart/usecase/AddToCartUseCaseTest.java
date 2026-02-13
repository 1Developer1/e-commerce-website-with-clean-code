package com.ecommerce.cart.usecase;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.usecase.ProductRepository;
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

class AddToCartUseCaseTest {

    private CartRepository cartRepository;
    private ProductRepository productRepository;
    private AddToCartUseCase addToCartUseCase;

    @BeforeEach
    void setUp() {
        cartRepository = Mockito.mock(CartRepository.class);
        productRepository = Mockito.mock(ProductRepository.class);
        addToCartUseCase = new AddToCartUseCase(cartRepository, productRepository);
    }

    @Test
    void shouldAddItemToNewCartIfCartDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "P1", "D1", Money.of(BigDecimal.TEN, "USD"), 10);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        AddToCartInput input = new AddToCartInput(userId, productId, 2);
        AddToCartOutput output = addToCartUseCase.execute(input);

        Assertions.assertTrue(output.success());
        Assertions.assertEquals(1, output.itemsCount());
        Assertions.assertEquals(new BigDecimal("20"), output.totalAmount());

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository, times(1)).save(cartCaptor.capture());
        Cart savedCart = cartCaptor.getValue();
        Assertions.assertEquals(userId, savedCart.getUserId());
        Assertions.assertEquals(1, savedCart.getItems().size());
    }

    @Test
    void shouldReturnErrorIfProductNotFound() {
         UUID userId = UUID.randomUUID();
         UUID productId = UUID.randomUUID();

         when(productRepository.findById(productId)).thenReturn(Optional.empty());

         AddToCartInput input = new AddToCartInput(userId, productId, 1);
         AddToCartOutput output = addToCartUseCase.execute(input);

         Assertions.assertFalse(output.success());
         Assertions.assertEquals("Product not found", output.message());
         verify(cartRepository, never()).save(any());
    }
}
