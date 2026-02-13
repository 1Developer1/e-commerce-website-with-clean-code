package com.ecommerce.cart.usecase;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.usecase.ProductRepository;
import java.util.Optional;

public class AddToCartUseCase {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public AddToCartUseCase(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public AddToCartOutput execute(AddToCartInput input) {
        if (input.quantity() <= 0) {
            return new AddToCartOutput(false, "Quantity must be positive", 0, null, null);
        }

        Optional<Product> productOpt = productRepository.findById(input.productId());
        if (productOpt.isEmpty()) {
            return new AddToCartOutput(false, "Product not found", 0, null, null);
        }
        Product product = productOpt.get();

        Cart cart = cartRepository.findByUserId(input.userId())
                .orElseGet(() -> Cart.create(input.userId()));

        try {
            cart.addItem(product, input.quantity());
            cartRepository.save(cart);
            
            return new AddToCartOutput(
                true, 
                "Item added successfully", 
                cart.getItems().size(), 
                cart.getTotalPrice().getAmount(), 
                cart.getTotalPrice().getCurrency()
            );
        } catch (Exception e) {
            return new AddToCartOutput(false, e.getMessage(), 0, null, null);
        }
    }
}
