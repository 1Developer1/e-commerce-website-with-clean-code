package com.ecommerce.shipping.adapter.out.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecommerce.shipping.usecase.port.ShippingProvider;
import com.ecommerce.infrastructure.tracing.TraceContextPropagator;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class DummyShippingProvider implements ShippingProvider {
    private static final Logger logger = LoggerFactory.getLogger(DummyShippingProvider.class);

    private final CircuitBreaker circuitBreaker;
    private final Bulkhead bulkhead;
    private final Retry retry;
    private final TimeLimiter timeLimiter;

    private final String apiUrl;
    private final TraceContextPropagator tracePropagator;

    public DummyShippingProvider(String apiUrl, TraceContextPropagator tracePropagator) {
        this.apiUrl = apiUrl;
        this.tracePropagator = tracePropagator;
        
        // Circuit Breaker: Açılması için %50 hata oranı veya yavaşlık
        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .slidingWindowSize(5)
            .build();
        this.circuitBreaker = CircuitBreaker.of("shippingService", cbConfig);

        // Bulkhead: Eşzamanlı en fazla 5 kargo isteği
        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
            .maxConcurrentCalls(5)
            .maxWaitDuration(Duration.ofMillis(500))
            .build();
        this.bulkhead = Bulkhead.of("shippingService", bulkheadConfig);

        // Retry: Exponential Backoff (1s, 2s, 4s...)
        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(1000))
            .failAfterMaxAttempts(true)
            .build();
        this.retry = Retry.of("shippingService", retryConfig);

        // TimeLimiter: Max 2 saniye bekle
        TimeLimiterConfig tlConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(2))
            .build();
        this.timeLimiter = TimeLimiter.of("shippingService", tlConfig);
    }

    @Override
    public String generateTrackingCode(String address) {
        // Dış çağrıyı temsil eden asenkron işlem (Timeout mekanizması bağlamak için Future kullanıyoruz)
        Callable<String> timeRestrictedCall = TimeLimiter.decorateFutureSupplier(timeLimiter, 
            () -> CompletableFuture.supplyAsync(() -> simulateExternalApi(address)));

        Callable<String> retriedCall = Retry.decorateCallable(retry, timeRestrictedCall);
        Callable<String> bulkheadedCall = Bulkhead.decorateCallable(bulkhead, retriedCall);
        Callable<String> decoratedCall = CircuitBreaker.decorateCallable(circuitBreaker, bulkheadedCall);

        try {
            return decoratedCall.call();
        } catch (Exception e) {
            logger.warn("[DummyShippingProvider-Fallback] Exception captured by Resilience4j: " + e.getMessage());
            return fallbackTrackingCode();
        }
    }

    private String simulateExternalApi(String address) {
        logger.info("[DummyShippingProvider] Dış API çağrılıyor... (Adres: " + address + ") to " + apiUrl);
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest.Builder requestBuilder = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(apiUrl + "/api/shipment"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString("{\"address\": \"" + address + "\"}"));

            // W3C Trace Context Propagation
            String traceparent = tracePropagator.getTraceparentHeader();
            if (!traceparent.isEmpty()) {
                requestBuilder.header("traceparent", traceparent);
            }
            java.net.http.HttpRequest request = requestBuilder.build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 500) {
                // Trigger circuit breaker or retry
                throw new RuntimeException("External shipping service returned 500");
            }
            return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Network/IO Error", e);
        }
    }

    private String fallbackTrackingCode() {
        logger.info("[DummyShippingProvider-Fallback] Kargo servisi yanıt vermiyor. Geçici kod üretildi.");
        return "TRK-FALLBACK-PENDING";
    }
}
