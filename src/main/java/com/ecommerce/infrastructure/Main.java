package com.ecommerce.infrastructure;

import com.ecommerce.cart.adapter.in.controller.CartController;
import com.ecommerce.cart.adapter.out.persistence.InMemoryCartRepository;
import com.ecommerce.cart.usecase.AddToCartInput;
import com.ecommerce.cart.usecase.AddToCartOutput;
import com.ecommerce.cart.usecase.AddToCartUseCase;
import com.ecommerce.cart.usecase.CartRepository;
import com.ecommerce.order.adapter.in.controller.OrderController;
import com.ecommerce.order.adapter.out.persistence.InMemoryOrderRepository;
import com.ecommerce.order.usecase.OrderRepository;
import com.ecommerce.order.usecase.PlaceOrderInput;
import com.ecommerce.order.usecase.PlaceOrderOutput;
import com.ecommerce.order.usecase.PlaceOrderUseCase;
import com.ecommerce.product.adapter.in.controller.ProductController;
import com.ecommerce.product.adapter.out.persistence.InMemoryProductRepository;
import com.ecommerce.product.usecase.CreateProductInput;
import com.ecommerce.product.usecase.CreateProductOutput;
import com.ecommerce.product.usecase.CreateProductUseCase;
import com.ecommerce.product.usecase.ListProductsOutput;
import com.ecommerce.product.usecase.ListProductsUseCase;
import com.ecommerce.product.usecase.ProductRepository;

import java.math.BigDecimal;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        // 1. Infrastructure / Persistence
        ProductRepository productRepository = new InMemoryProductRepository();
        CartRepository cartRepository = new InMemoryCartRepository();
        OrderRepository orderRepository = new InMemoryOrderRepository();

        // 2. Use Cases
        CreateProductUseCase createProductUseCase = new CreateProductUseCase(productRepository);
        ListProductsUseCase listProductsUseCase = new ListProductsUseCase(productRepository);
        AddToCartUseCase addToCartUseCase = new AddToCartUseCase(cartRepository, productRepository);
        PlaceOrderUseCase placeOrderUseCase = new PlaceOrderUseCase(orderRepository, cartRepository);

        // 3. Interface Adapters / Controllers
        ProductController productController = new ProductController(createProductUseCase, listProductsUseCase);
        CartController cartController = new CartController(addToCartUseCase);
        OrderController orderController = new OrderController(placeOrderUseCase);

        // 4. Scenario Execution
        System.out.println("--- E-Commerce System Started ---");

        // Step 1: Create Product
        System.out.println("\n[1] Creating Product...");
        CreateProductInput productInput = new CreateProductInput(
            "MacBook Pro", "High-end laptop", new BigDecimal("1999.99"), "USD", 10
        );
        CreateProductOutput productOutput = productController.createProduct(productInput);
        System.out.println("Product Created: " + productOutput.name() + " (ID: " + productOutput.id() + ")");

        // Step 2: List Products
        System.out.println("\n[2] Listing Products...");
        ListProductsOutput listOutput = productController.listProducts();
        listOutput.products().forEach(p -> 
            System.out.println(" - " + p.name() + ": " + p.price() + " " + p.currency())
        );

        // Step 3: Add to Cart
        System.out.println("\n[3] Adding to Cart...");
        UUID userId = UUID.randomUUID();
        AddToCartInput cartInput = new AddToCartInput(userId, productOutput.id(), 1);
        AddToCartOutput cartOutput = cartController.addToCart(cartInput);
        System.out.println("Cart: " + cartOutput.itemsCount() + " items, Total: " + cartOutput.totalAmount() + " " + cartOutput.currency());

        // Step 4: Place Order
        System.out.println("\n[4] Placing Order...");
        PlaceOrderInput orderInput = new PlaceOrderInput(userId);
        PlaceOrderOutput orderOutput = orderController.placeOrder(orderInput);
        System.out.println("Order Placed: ID=" + orderOutput.orderId() + ", Status=" + orderOutput.status() + ", Total=" + orderOutput.totalAmount());
        
        System.out.println("\n--- Scenario Completed ---");
    }
}
