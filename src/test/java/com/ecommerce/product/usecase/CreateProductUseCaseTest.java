package com.ecommerce.product.usecase;

import com.ecommerce.product.entity.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class CreateProductUseCaseTest {

    private ProductRepository productRepository;
    private CreateProductUseCase createProductUseCase;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        createProductUseCase = new CreateProductUseCase(productRepository);
    }

    @Test
    void shouldCreateProductSuccessfully() {
        CreateProductInput input = new CreateProductInput(
            "Test Product",
            "Description",
            new BigDecimal("100.00"),
            "USD",
            10
        );

        CreateProductOutput output = createProductUseCase.execute(input);

        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.success());
        Assertions.assertNotNull(output.id());
        Assertions.assertEquals("Test Product", output.name());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        Assertions.assertEquals("Test Product", savedProduct.getName());
        Assertions.assertEquals(new BigDecimal("100.00"), savedProduct.getPrice().getAmount());
    }
}
