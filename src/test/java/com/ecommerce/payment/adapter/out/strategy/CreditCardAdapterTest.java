package com.ecommerce.payment.adapter.out.strategy;

import com.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreditCardAdapterTest {

    private CreditCardAdapter adapter;
    private Money testAmount;

    @BeforeEach
    void setUp() {
        adapter = new CreditCardAdapter();
        testAmount = Money.of(new BigDecimal("100.00"), "USD");
    }

    @Test
    void testSuccessfulPayment() {
        // Given normal conditions, the adapter should return true
        boolean result = adapter.pay(testAmount);
        assertTrue(result, "Normal payment should succeed.");
    }
    
    @Test
    void testTimeLimiterFallbackTrigger() {
        // We'll test the fallback mechanism by creating a subclass that forces a delay.
        // Wait duration > 3 seconds will trigger the Resilience4j TimeLimiter.
        // HOWEVER, because we also have a Retry(maxAttempts=3) configured AROUND the TimeLimiter,
        // it will retry the TimeoutException 3 times before finally giving up.
        // Total expected time: 3s + 500ms + 3s + 500ms + 3s = ~10 seconds.
        CreditCardAdapter slowAdapter = new CreditCardAdapter() {
            @Override
            protected boolean simulateExternalPayment(Money amount) {
                try {
                    Thread.sleep(4000); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return true;
            }
        };

        long startTime = System.currentTimeMillis();
        boolean result = slowAdapter.pay(testAmount);
        long duration = System.currentTimeMillis() - startTime;

        assertFalse(result, "Slow payment should exhaust retries, trigger fallback and return false.");
        assertTrue(duration >= 9000, "Adapter should take ~10s due to 3 TimeLimiter strikes + Retry backoffs.");
    }

    @Test
    void testCircuitBreakerOpensAfterFailures() {
        // Test CircuitBreaker by forcing failures to cross the 50% rate threshold.
        CreditCardAdapter failingAdapter = new CreditCardAdapter() {
            @Override
            protected boolean simulateExternalPayment(Money amount) {
                 // Always fail this mock so Retry exhausts itself cleanly and Circuit receives failures.
                 throw new RuntimeException("Simulated External API Crash");
            }
        };

        // 1. Send enough requests to fill the sliding window (size 10) and trip the Circuit Breaker.
        // Note: each call here invokes the provider 3 times due to Retry, so 4 loop iterations is 12 failures!
        for (int i = 0; i < 4; i++) {
            boolean result = failingAdapter.pay(testAmount);
            assertFalse(result, "Failed API call should default to fallback (false)");
        }

        // 2. The next requests will immediately be rejected by the CircuitBreaker (CallNotPermittedException)
        // because Circuit is now OPEN. It won't even wait for Retry.
        
        long startTime = System.currentTimeMillis();
        boolean resultAfterOpen = failingAdapter.pay(testAmount);
        long duration = System.currentTimeMillis() - startTime;
        
        // Assert it instantly failed without full timeouts
        assertFalse(resultAfterOpen, "Call should be rejected immediately because Circuit is OPEN.");
        assertTrue(duration < 1500, "Fast-Fail should completely skip 3-second TimeLimiter.");
    }
}
