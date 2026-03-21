package com.ecommerce.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.ecommerce.cart.adapter.out.persistence.InMemoryCartRepository;
import com.ecommerce.cart.usecase.AddToCartUseCase;
import com.ecommerce.cart.usecase.GetCartUseCase;
import com.ecommerce.cart.usecase.CartRepository;
import com.ecommerce.cart.usecase.ApplyDiscountUseCase;
import com.ecommerce.cart.usecase.port.DiscountProvider;
import com.ecommerce.cart.api.CartService;
import com.ecommerce.cart.internal.CartModule;

import com.ecommerce.order.adapter.out.persistence.InMemoryOrderRepository;
import com.ecommerce.order.usecase.GetOrderByIdUseCase;
import com.ecommerce.order.usecase.GetOrdersUseCase;
import com.ecommerce.order.usecase.OrderRepository;
import com.ecommerce.order.usecase.PlaceOrderUseCase;
import com.ecommerce.order.adapter.in.event.OrderPaymentEventHandler;

import com.ecommerce.shipping.adapter.in.event.OrderPaidEventHandler;

import com.ecommerce.product.adapter.out.persistence.InMemoryProductRepository;
import com.ecommerce.product.usecase.CreateProductUseCase;
import com.ecommerce.product.usecase.ListProductsUseCase;
import com.ecommerce.product.usecase.ProductRepository;

import com.ecommerce.discount.usecase.DiscountRepository;
import com.ecommerce.discount.adapter.out.persistence.InMemoryDiscountRepository;
import com.ecommerce.discount.usecase.GetDiscountUseCase;
import com.ecommerce.discount.usecase.GetDiscountInput;
import com.ecommerce.discount.usecase.GetDiscountOutput;

import com.ecommerce.payment.usecase.port.PaymentGateway;
import com.ecommerce.payment.adapter.out.strategy.CreditCardAdapter;
import com.ecommerce.payment.adapter.out.strategy.BankTransferAdapter;
import com.ecommerce.payment.usecase.PayOrderUseCase;

import com.ecommerce.shipping.usecase.port.ShippingRepository;
import com.ecommerce.shipping.adapter.out.persistence.InMemoryShipmentRepository;
import com.ecommerce.shipping.usecase.port.ShippingProvider;
import com.ecommerce.shipping.adapter.out.provider.DummyShippingProvider;
import com.ecommerce.shipping.usecase.CreateShipmentUseCase;
import com.ecommerce.shipping.api.ShippingService;
import com.ecommerce.shipping.internal.ShippingModule;
import com.ecommerce.shared.event.EventBus;
import com.ecommerce.shared.event.SimpleEventBus;
import com.ecommerce.infrastructure.tracing.TraceContextPropagator;

import com.ecommerce.product.adapter.in.controller.ProductController;
import com.ecommerce.cart.adapter.in.controller.CartController;
import com.ecommerce.order.adapter.in.controller.OrderController;
import com.ecommerce.payment.adapter.in.controller.PaymentController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class UseCaseConfig {

    @Bean
    public EventBus eventBus() {
        return new SimpleEventBus();
    }

    // --- REPOSITORIES (In-Memory for now) ---
    // Product, Order and Cart repositories are now provided by JPA Persistence Adapters via @Component scanning


    @Bean
    public ShippingRepository shippingRepository() {
        return new InMemoryShipmentRepository();
    }

    // --- USE CASES & SERVICES ---
    @Bean
    public CreateProductUseCase createProductUseCase(ProductRepository productRepository) {
        return new CreateProductUseCase(productRepository);
    }

    @Bean
    public ListProductsUseCase listProductsUseCase(ProductRepository productRepository) {
        return new ListProductsUseCase(productRepository);
    }

    @Bean
    public com.ecommerce.product.usecase.UpdateProductUseCase updateProductUseCase(ProductRepository productRepository) {
        return new com.ecommerce.product.usecase.UpdateProductUseCase(productRepository);
    }

    @Bean
    public com.ecommerce.product.usecase.DeleteProductUseCase deleteProductUseCase(ProductRepository productRepository) {
        return new com.ecommerce.product.usecase.DeleteProductUseCase(productRepository);
    }

    @Bean
    public com.ecommerce.product.usecase.DeductProductStockUseCase deductProductStockUseCase(ProductRepository productRepository) {
        return new com.ecommerce.product.usecase.DeductProductStockUseCase(productRepository);
    }

    @Bean
    public GetDiscountUseCase getDiscountUseCase(DiscountRepository discountRepository) {
        return new GetDiscountUseCase(discountRepository);
    }

    @Bean
    public AddToCartUseCase addToCartUseCase(CartRepository cartRepository, ProductRepository productRepository) {
        return new AddToCartUseCase(cartRepository, productRepository);
    }

    @Bean
    public GetCartUseCase getCartUseCase(CartRepository cartRepository) {
        return new GetCartUseCase(cartRepository);
    }

    @Bean
    public DiscountProvider discountProvider(GetDiscountUseCase getDiscountUseCase) {
        return (cart, code) -> {
            GetDiscountOutput output = getDiscountUseCase.execute(new GetDiscountInput(code));
            return output.isValid() ? java.util.Optional.of(output.amount()) : java.util.Optional.empty();
        };
    }

    @Bean
    public ApplyDiscountUseCase applyDiscountUseCase(CartRepository cartRepository, DiscountProvider discountProvider) {
        return new ApplyDiscountUseCase(cartRepository, discountProvider);
    }

    @Bean
    public CartService cartService(CartRepository cartRepository) {
        return CartModule.createService(cartRepository);
    }

    @Bean
    public PlaceOrderUseCase placeOrderUseCase(OrderRepository orderRepository, CartService cartService, com.ecommerce.shared.event.EventBus eventBus) {
        return new PlaceOrderUseCase(orderRepository, cartService, eventBus);
    }

    @Bean
    public GetOrdersUseCase getOrdersUseCase(OrderRepository orderRepository) {
        return new GetOrdersUseCase(orderRepository);
    }

    @Bean
    public GetOrderByIdUseCase getOrderByIdUseCase(OrderRepository orderRepository) {
        return new GetOrderByIdUseCase(orderRepository);
    }

    @Bean
    public Map<String, PaymentGateway> paymentStrategies(TraceContextPropagator tracePropagator) {
        String paymentApiUrl = System.getenv("PAYMENT_API_URL") != null ? System.getenv("PAYMENT_API_URL") : "http://localhost:8081";
        Map<String, PaymentGateway> strategies = new HashMap<>();
        strategies.put("CREDIT_CARD", new CreditCardAdapter(paymentApiUrl, tracePropagator));
        strategies.put("BANK_TRANSFER", new BankTransferAdapter());
        return strategies;
    }

    @Bean
    public PayOrderUseCase payOrderUseCase(Map<String, PaymentGateway> paymentStrategies, EventBus eventBus) {
        return new PayOrderUseCase(paymentStrategies, eventBus);
    }

    @Bean
    public ShippingProvider shippingProvider(TraceContextPropagator tracePropagator) {
        String shippingApiUrl = System.getenv("SHIPPING_API_URL") != null ? System.getenv("SHIPPING_API_URL") : "http://localhost:8081";
        return new DummyShippingProvider(shippingApiUrl, tracePropagator);
    }

    @Bean
    public CreateShipmentUseCase createShipmentUseCase(ShippingRepository shippingRepository, ShippingProvider shippingProvider) {
        return new CreateShipmentUseCase(shippingRepository, shippingProvider);
    }

    @Bean
    public ShippingService shippingService(ShippingRepository shippingRepository, CreateShipmentUseCase createShipmentUseCase) {
        return ShippingModule.createService(shippingRepository, createShipmentUseCase);
    }

    // --- EVENT HANDLERS ---
    @Bean
    public OrderPaymentEventHandler orderPaymentEventHandler(OrderRepository orderRepository, EventBus eventBus) {
        return new OrderPaymentEventHandler(orderRepository, eventBus);
    }

    @Bean
    public OrderPaidEventHandler orderPaidEventHandler(ShippingService shippingService, EventBus eventBus) {
        return new OrderPaidEventHandler(shippingService, eventBus);
    }

    // --- PRESENTERS (Interface Adapters) ---
    @Bean
    public com.ecommerce.product.adapter.in.presenter.ProductPresenter productPresenter() {
        return new com.ecommerce.product.adapter.in.presenter.ProductPresenter();
    }

    @Bean
    public com.ecommerce.order.adapter.in.presenter.OrderPresenter orderPresenter() {
        return new com.ecommerce.order.adapter.in.presenter.OrderPresenter();
    }

    // Controllers are now decorated with @RestController and auto-discovered by Spring Boot.
}
