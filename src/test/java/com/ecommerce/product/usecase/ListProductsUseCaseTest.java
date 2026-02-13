package com.ecommerce.product.usecase;

import com.ecommerce.product.entity.Product;
import com.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;

class ListProductsUseCaseTest {

    private ProductRepository productRepository;
    private ListProductsUseCase listProductsUseCase;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        listProductsUseCase = new ListProductsUseCase(productRepository);
    }

    @Test
    void shouldReturnListOfProducts() {
        Product p1 = Product.create("P1", "D1", Money.of(BigDecimal.TEN, "USD"), 10);
        Product p2 = Product.create("P2", "D2", Money.of(BigDecimal.ONE, "USD"), 5);

        when(productRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        ListProductsOutput output = listProductsUseCase.execute();

        Assertions.assertNotNull(output);
        Assertions.assertEquals(2, output.products().size());
        Assertions.assertEquals("P1", output.products().get(0).name());
        Assertions.assertEquals("P2", output.products().get(1).name());
    }

    @Test
    void shouldReturnEmptyListWhenNoProducts() {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        ListProductsOutput output = listProductsUseCase.execute();

        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.products().isEmpty());
    }
}
