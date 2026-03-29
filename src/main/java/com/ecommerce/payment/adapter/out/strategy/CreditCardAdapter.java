package com.ecommerce.payment.adapter.out.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecommerce.payment.usecase.port.PaymentGateway;
import com.ecommerce.shared.domain.Money;
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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class CreditCardAdapter implements PaymentGateway {
    private static final Logger logger = LoggerFactory.getLogger(CreditCardAdapter.class);

    private final CircuitBreaker circuitBreaker;
    private final Bulkhead bulkhead;
    private final Retry retry;
    private final TimeLimiter timeLimiter;

    private final String apiUrl;
    private final TraceContextPropagator tracePropagator;

    public CreditCardAdapter(String apiUrl, TraceContextPropagator tracePropagator) {
        this.apiUrl = apiUrl;
        this.tracePropagator = tracePropagator;

        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(15))
            .slidingWindowSize(10)
            .build();
        this.circuitBreaker = CircuitBreaker.of("creditCardService", cbConfig);

        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
            .maxConcurrentCalls(10)
            .maxWaitDuration(Duration.ofMillis(100))
            .build();
        this.bulkhead = Bulkhead.of("creditCardService", bulkheadConfig);

        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(500))
            .build();
        this.retry = Retry.of("creditCardService", retryConfig);

        TimeLimiterConfig tlConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(3))
            .build();
        this.timeLimiter = TimeLimiter.of("creditCardService", tlConfig);
    }

    @Override
    public boolean pay(Money amount) {
        Callable<Boolean> timeRestrictedCall = TimeLimiter.decorateFutureSupplier(timeLimiter, 
            () -> CompletableFuture.supplyAsync(() -> simulateExternalPayment(amount)));

        Callable<Boolean> retriedCall = Retry.decorateCallable(retry, timeRestrictedCall);
        Callable<Boolean> bulkheadedCall = Bulkhead.decorateCallable(bulkhead, retriedCall);
        Callable<Boolean> decoratedCall = CircuitBreaker.decorateCallable(circuitBreaker, bulkheadedCall);

        try {
            return decoratedCall.call();
        } catch (Exception e) {
            logger.warn("[CreditCardAdapter-Fallback] Exception captured by Resilience4j: " + e.getMessage());
            return fallbackPayment();
        }
    }

    protected boolean simulateExternalPayment(Money amount) {
        logger.info("[CreditCardAdapter] Dış Ödeme API'si çağrılıyor... (" + amount + ") to " + apiUrl);
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest.Builder requestBuilder = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(apiUrl + "/api/charge"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString("{\"amount\": " + amount.getAmount() + "}"));

            // W3C Trace Context Propagation
            String traceparent = tracePropagator.getTraceparentHeader();
            if (!traceparent.isEmpty()) {
                requestBuilder.header("traceparent", traceparent);
            }
            java.net.http.HttpRequest request = requestBuilder.build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return true;
            }
            throw new RuntimeException("Payment failed with HTTP status: " + response.statusCode());
        } catch (Exception e) {
            logger.error("[CreditCardAdapter] Network/IO Error: {} - {}", e.getClass().getName(), e.getMessage(), e);
            throw new RuntimeException("Network/IO Error: " + e.getClass().getName() + " - " + e.getMessage(), e);
        }
    }

    private boolean fallbackPayment() {
        logger.info("[CreditCardAdapter-Fallback] Ödeme sistemi kapalı veya yoğun. İşlem reddedildi.");
        return false;
    }
}
