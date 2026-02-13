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

import com.ecommerce.discount.usecase.DiscountRepository;
import com.ecommerce.discount.adapter.out.persistence.InMemoryDiscountRepository;
import com.ecommerce.discount.usecase.GetDiscountUseCase;
import com.ecommerce.discount.usecase.GetDiscountInput;
import com.ecommerce.discount.usecase.GetDiscountOutput;
import com.ecommerce.cart.usecase.port.DiscountProvider;
import com.ecommerce.cart.usecase.ApplyDiscountUseCase;
import com.ecommerce.cart.usecase.ApplyDiscountInput;
import com.ecommerce.cart.usecase.ApplyDiscountOutput;

import com.ecommerce.payment.usecase.port.PaymentGateway;
import com.ecommerce.payment.adapter.out.strategy.CreditCardAdapter;
import com.ecommerce.payment.adapter.out.strategy.BankTransferAdapter;
import com.ecommerce.payment.usecase.PayOrderUseCase;
import com.ecommerce.payment.usecase.PayOrderInput;
import com.ecommerce.payment.usecase.PayOrderOutput;
import com.ecommerce.payment.adapter.in.controller.PaymentController;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        // 1. Infrastructure / Persistence
        ProductRepository productRepository = new InMemoryProductRepository();
        CartRepository cartRepository = new InMemoryCartRepository();
        OrderRepository orderRepository = new InMemoryOrderRepository();

        // 2. Use Cases
        CreateProductUseCase createProductUseCase = new CreateProductUseCase(productRepository);
        ListProductsUseCase listProductsUseCase = new ListProductsUseCase(productRepository);
        
        // Discount Module Wiring
        DiscountRepository discountRepository = new InMemoryDiscountRepository();
        // Seed a discount code
        discountRepository.save(com.ecommerce.discount.entity.Discount.create("SUMMER10", com.ecommerce.shared.domain.Money.of(new BigDecimal("10.00"), "USD")));
        
        GetDiscountUseCase getDiscountUseCase = new GetDiscountUseCase(discountRepository);
        
        // Adapter: Cart -> Discount
        DiscountProvider discountProvider = code -> {
            GetDiscountOutput output = getDiscountUseCase.execute(new com.ecommerce.discount.usecase.GetDiscountInput(code));
            return output.isValid() ? java.util.Optional.of(output.amount()) : java.util.Optional.empty();
        };

        AddToCartUseCase addToCartUseCase = new AddToCartUseCase(cartRepository, productRepository);
        ApplyDiscountUseCase applyDiscountUseCase = new ApplyDiscountUseCase(cartRepository, discountProvider);
        PlaceOrderUseCase placeOrderUseCase = new PlaceOrderUseCase(orderRepository, cartRepository);

        // Payment Module Wiring
        Map<String, PaymentGateway> paymentStrategies = new HashMap<>();
        paymentStrategies.put("CREDIT_CARD", new CreditCardAdapter());
        paymentStrategies.put("BANK_TRANSFER", new BankTransferAdapter());
        
        PayOrderUseCase payOrderUseCase = new PayOrderUseCase(orderRepository, paymentStrategies);

        // 3. Interface Adapters / Controllers
        ProductController productController = new ProductController(createProductUseCase, listProductsUseCase);
        CartController cartController = new CartController(addToCartUseCase, applyDiscountUseCase);
        OrderController orderController = new OrderController(placeOrderUseCase);
        PaymentController paymentController = new PaymentController(payOrderUseCase);

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

        // Step 4: Apply Discount
        System.out.println("\n[4] Applying Discount...");
        ApplyDiscountInput discountInput = new ApplyDiscountInput(userId, "SUMMER10");
        ApplyDiscountOutput discountOutput = cartController.applyDiscount(discountInput);
        System.out.println("Discount Applied: " + discountOutput.success() + " (" + discountOutput.message() + ")");
        System.out.println("New Cart Total: " + discountOutput.newTotal());

        // Step 5: Place Order
        System.out.println("\n[5] Placing Order...");
        PlaceOrderInput orderInput = new PlaceOrderInput(userId);
        PlaceOrderOutput orderOutput = orderController.placeOrder(orderInput);
        System.out.println("Order Placed: ID=" + orderOutput.orderId() + ", Status=" + orderOutput.status() + ", Total=" + orderOutput.totalAmount());
        
        // Step 6: Pay Order
        System.out.println("\n[6] Paying Order...");
        PayOrderInput payInput = new PayOrderInput(orderOutput.orderId(), "CREDIT_CARD");
        PayOrderOutput payOutput = paymentController.payOrder(payInput);
        System.out.println("Payment Result: " + payOutput.success() + " (" + payOutput.message() + ")");
        
        System.out.println("\n--- Scenario Completed ---");
    }
}
