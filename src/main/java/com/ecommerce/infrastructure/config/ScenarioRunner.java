package com.ecommerce.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ecommerce.product.usecase.CreateProductInput;
import com.ecommerce.product.usecase.CreateProductOutput;
import com.ecommerce.product.usecase.CreateProductUseCase;
import com.ecommerce.product.usecase.ListProductsOutput;
import com.ecommerce.product.usecase.ListProductsUseCase;
import com.ecommerce.cart.adapter.in.controller.CartController;
import com.ecommerce.cart.adapter.in.controller.AddToCartRequest;
import com.ecommerce.cart.adapter.in.controller.ApplyDiscountRequest;
import com.ecommerce.cart.usecase.AddToCartOutput;
import com.ecommerce.cart.usecase.ApplyDiscountOutput;
import com.ecommerce.order.usecase.PlaceOrderInput;
import com.ecommerce.order.usecase.PlaceOrderOutput;
import com.ecommerce.order.usecase.PlaceOrderUseCase;
import com.ecommerce.payment.adapter.in.controller.PaymentController;
import com.ecommerce.payment.adapter.in.controller.PayOrderRequest;
import com.ecommerce.payment.usecase.PayOrderInput;
import com.ecommerce.payment.usecase.PayOrderOutput;
import com.ecommerce.shipping.api.ShippingService;
import java.util.Map;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class ScenarioRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioRunner.class);

    private final CreateProductUseCase createProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final CartController cartController;
    private final PlaceOrderUseCase placeOrderUseCase;
    private final PaymentController paymentController;
    private final ShippingService shippingService;

    public ScenarioRunner(CreateProductUseCase createProductUseCase, ListProductsUseCase listProductsUseCase,
                          CartController cartController, PlaceOrderUseCase placeOrderUseCase,
                          PaymentController paymentController, ShippingService shippingService) {
        this.createProductUseCase = createProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.cartController = cartController;
        this.placeOrderUseCase = placeOrderUseCase;
        this.paymentController = paymentController;
        this.shippingService = shippingService;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("--- E-Commerce System Started (Spring Boot) ---");

        // Step 1: Create Product (Use Case directly to get typed output)
        logger.info("\n[1] Creating Product...");
        CreateProductInput productInput = new CreateProductInput(
            "MacBook Pro", "High-end laptop", new BigDecimal("1999.99"), "USD", 10
        );
        CreateProductOutput productOutput = createProductUseCase.execute(productInput);
        logger.info("Product Created: " + productOutput.name() + " (ID: " + productOutput.id() + ")");

        // Step 2: List Products
        logger.info("\n[2] Listing Products...");
        ListProductsOutput listOutput = listProductsUseCase.execute();
        listOutput.products().forEach(p -> 
            logger.info(" - " + p.name() + ": " + p.price() + " " + p.currency())
        );

        // Step 3: Add to Cart
        logger.info("\n[3] Adding to Cart...");
        UUID userId = UUID.randomUUID();
        AddToCartRequest cartRequest = new AddToCartRequest(productOutput.id(), 1);
        Map<String, Object> cartOutput = cartController.addToCart(userId, cartRequest).getBody();
        logger.info("Cart Updated: " + cartOutput.get("success") + " - " + cartOutput.get("message"));

        // Step 4: Apply Discount
        logger.info("\n[4] Applying Discount...");
        ApplyDiscountRequest discountRequest = new ApplyDiscountRequest("SUMMER10");
        Map<String, Object> discountOutput = cartController.applyDiscount(userId, discountRequest).getBody();
        logger.info("Discount Applied: " + discountOutput.get("success") + " (" + discountOutput.get("message") + ")");

        // Step 5: Place Order (Use Case directly to get typed output for payment)
        logger.info("\n[5] Placing Order...");
        PlaceOrderOutput orderOutput = placeOrderUseCase.execute(new PlaceOrderInput(userId));
        logger.info("Order Placed: ID=" + orderOutput.orderId() + ", Status=" + orderOutput.status() + ", Total=" + orderOutput.totalAmount());
        
        // Step 6: Pay Order
        logger.info("\n[6] Paying Order...");
        PayOrderRequest payRequest = new PayOrderRequest(orderOutput.orderId(), orderOutput.totalAmount(), "USD", "CREDIT_CARD");
        Map<String, Object> payOutput = paymentController.payOrder(userId, payRequest).getBody();
        logger.info("Payment Result: " + payOutput.get("success") + " (" + payOutput.get("message") + ")");
        
        // Step 7: Track Shipment
        logger.info("\n[7] Tracking Shipment...");
        java.util.Optional<com.ecommerce.shipping.api.dto.ShipmentDto> shipmentOpt = shippingService.trackShipment(orderOutput.orderId());
        shipmentOpt.ifPresentOrElse(
            s -> logger.info("Shipment Tracking: " + s.trackingCode() + " [" + s.status() + "] to " + s.address()),
            () -> logger.info("Shipment not found yet.")
        );
        
        logger.info("\n--- Scenario Completed ---");
    }
}
