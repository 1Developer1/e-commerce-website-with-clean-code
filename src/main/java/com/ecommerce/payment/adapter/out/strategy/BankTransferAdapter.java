package com.ecommerce.payment.adapter.out.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecommerce.payment.usecase.port.PaymentGateway;
import com.ecommerce.shared.domain.Money;
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

public class BankTransferAdapter implements PaymentGateway {
    private static final Logger logger = LoggerFactory.getLogger(BankTransferAdapter.class);

    private final CircuitBreaker circuitBreaker;
    private final Bulkhead bulkhead;
    private final Retry retry;
    private final TimeLimiter timeLimiter;

    public BankTransferAdapter() {
        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(15))
            .slidingWindowSize(10)
            .build();
        this.circuitBreaker = CircuitBreaker.of("bankTransferService", cbConfig);

        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
            .maxConcurrentCalls(10)
            .maxWaitDuration(Duration.ofMillis(100))
            .build();
        this.bulkhead = Bulkhead.of("bankTransferService", bulkheadConfig);

        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(500))
            .build();
        this.retry = Retry.of("bankTransferService", retryConfig);

        TimeLimiterConfig tlConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(3))
            .build();
        this.timeLimiter = TimeLimiter.of("bankTransferService", tlConfig);
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
            return fallbackPayment();
        }
    }

    private boolean simulateExternalPayment(Money amount) {
        logger.info("[BankTransferAdapter] Dış Banka API'si çağrılıyor... (" + amount + ")");
        return true; 
    }

    private boolean fallbackPayment() {
        logger.info("[BankTransferAdapter-Fallback] Banka sistemi kapalı veya yoğun. İşlem reddedildi.");
        return false;
    }
}
