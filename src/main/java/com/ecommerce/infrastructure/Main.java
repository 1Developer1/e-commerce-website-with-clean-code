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

import com.ecommerce.cart.api.CartService;
import com.ecommerce.cart.internal.CartModule;

import com.ecommerce.shared.event.EventBus;
import com.ecommerce.shared.event.SimpleEventBus;
import com.ecommerce.order.adapter.in.event.OrderPaymentEventHandler;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        // 0. Shared Infrastructure
        EventBus eventBus = new SimpleEventBus();

        // 1. Infrastructure / Persistence
        ProductRepository productRepository = new InMemoryProductRepository();
        CartRepository cartRepository = new InMemoryCartRepository();
        OrderRepository orderRepository = new InMemoryOrderRepository();
        
        // Event Handlers
        new OrderPaymentEventHandler(orderRepository, eventBus);

        // 2. Use Cases
        CreateProductUseCase createProductUseCase = new CreateProductUseCase(productRepository);
        ListProductsUseCase listProductsUseCase = new ListProductsUseCase(productRepository);
        
        // Discount Module Wiring
        DiscountRepository discountRepository = new InMemoryDiscountRepository();
        // Seed a discount code
        discountRepository.save(com.ecommerce.discount.entity.Discount.create("SUMMER10", com.ecommerce.shared.domain.Money.of(new BigDecimal("10.00"), "USD")));
        
        GetDiscountUseCase getDiscountUseCase = new GetDiscountUseCase(discountRepository);
        
        // Adapter: Cart -> Discount
        DiscountProvider discountProvider = (cart, code) -> {
            GetDiscountOutput output = getDiscountUseCase.execute(new com.ecommerce.discount.usecase.GetDiscountInput(code));
            
            // Logic can be extended here: e.g. percentage calculation based on cart.getTotalPrice()
            // For now, it remains fixed amount from Discount Entity
            return output.isValid() ? java.util.Optional.of(output.amount()) : java.util.Optional.empty();
        };

        // Services / Facades
        CartService cartService = CartModule.createService(cartRepository);

        AddToCartUseCase addToCartUseCase = new AddToCartUseCase(cartRepository, productRepository);
        ApplyDiscountUseCase applyDiscountUseCase = new ApplyDiscountUseCase(cartRepository, discountProvider);
        // Inject CartService Facade instead of CartRepository
        PlaceOrderUseCase placeOrderUseCase = new PlaceOrderUseCase(orderRepository, cartService);

        // Payment Module Wiring
        Map<String, PaymentGateway> paymentStrategies = new HashMap<>();
        paymentStrategies.put("CREDIT_CARD", new CreditCardAdapter());
        paymentStrategies.put("BANK_TRANSFER", new BankTransferAdapter());
        
        // Inject EventBus instead of just Repos. OrderRepository is REMOVED.
        PayOrderUseCase payOrderUseCase = new PayOrderUseCase(paymentStrategies, eventBus);

        // Shipping Module Wiring (Event Driven + API)
        com.ecommerce.shipping.usecase.port.ShippingRepository shippingRepository = new com.ecommerce.shipping.adapter.out.persistence.InMemoryShipmentRepository();
        com.ecommerce.shipping.usecase.port.ShippingProvider shippingProvider = new com.ecommerce.shipping.adapter.out.provider.DummyShippingProvider();
        
        // UseCases (Still instantiated for internal use by Factory)
        com.ecommerce.shipping.usecase.CreateShipmentUseCase createShipmentUseCase = new com.ecommerce.shipping.usecase.CreateShipmentUseCase(shippingRepository, shippingProvider);
        
        // Shipping API (Facade) - Created via Factory
        com.ecommerce.shipping.api.ShippingService shippingService = com.ecommerce.shipping.internal.ShippingModule.createService(shippingRepository, createShipmentUseCase);
        
        // Handler registers itself to EventBus (Now uses Service)
        new com.ecommerce.shipping.adapter.in.event.OrderPaidEventHandler(shippingService, eventBus);

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
        // Assuming Order Output gives total amount as BigDecimal, creating Money object
        PayOrderInput payInput = new PayOrderInput(orderOutput.orderId(), com.ecommerce.shared.domain.Money.of(orderOutput.totalAmount(), "USD"), "CREDIT_CARD");
        PayOrderOutput payOutput = paymentController.payOrder(payInput);
        System.out.println("Payment Result: " + payOutput.success() + " (" + payOutput.message() + ")");
        
        // Step 7: Track Shipment
        System.out.println("\n[7] Tracking Shipment...");
        // Wait a bit for async event processing (in real world). Here it's synchronous but good to check.
        java.util.Optional<com.ecommerce.shipping.api.dto.ShipmentDto> shipmentOpt = shippingService.trackShipment(orderOutput.orderId());
        shipmentOpt.ifPresentOrElse(
            s -> System.out.println("Shipment Tracking: " + s.trackingCode() + " [" + s.status() + "] to " + s.address()),
            () -> System.out.println("Shipment not found yet.")
        );
        
        System.out.println("\n--- Scenario Completed ---");
    }
}
