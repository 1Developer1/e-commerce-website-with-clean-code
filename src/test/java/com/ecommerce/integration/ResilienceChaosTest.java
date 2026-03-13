package com.ecommerce.integration;

import com.ecommerce.payment.usecase.port.PaymentGateway;
import com.ecommerce.shared.domain.Money;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = com.ecommerce.infrastructure.Main.class)
@TestPropertySource(properties = {
        "PAYMENT_API_URL=http://localhost:8081",
        "SHIPPING_API_URL=http://localhost:8081"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ResilienceChaosTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private java.util.Map<String, PaymentGateway> paymentStrategies;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8081));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8081);
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    @Test
    void testCreditCardPayment_Success() {
        // Mock a normal fast response
        stubFor(post(urlEqualTo("/api/charge"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"SUCCESS\"}")));

        PaymentGateway creditCardAdapter = paymentStrategies.get("CREDIT_CARD");
        boolean result = creditCardAdapter.pay(Money.of(new BigDecimal("100.00"), "USD"));
        
        assertTrue(result, "Payment should succeed when external API returns 200 within timeout limit.");
    }

    @Test
    void testCreditCardPayment_TimeoutFallback() {
        // Mock a slow API that takes 5 seconds (our TimeLimiter stops it at 3 seconds)
        stubFor(post(urlEqualTo("/api/charge"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(5000)
                        .withBody("{\"status\": \"SUCCESS\"}")));

        PaymentGateway creditCardAdapter = paymentStrategies.get("CREDIT_CARD");
        boolean result = creditCardAdapter.pay(Money.of(new BigDecimal("100.00"), "USD"));
        
        assertFalse(result, "TimeLimiter should interrupt the payment and fallback method should return false.");
    }

    @Test
    void testCreditCardPayment_CircuitBreakerOpens() {
        // Mock an API that is completely down (HTTP 500)
        stubFor(post(urlEqualTo("/api/charge"))
                .willReturn(aResponse().withStatus(500)));

        PaymentGateway creditCardAdapter = paymentStrategies.get("CREDIT_CARD");

        // We configured CircuitBreaker with slidingWindowSize = 10, failureRate = 50%
        // We will bombard it with 10 failed requests
        for (int i = 0; i < 10; i++) {
            boolean iterationResult = creditCardAdapter.pay(Money.of(new BigDecimal("10.00"), "USD"));
            assertFalse(iterationResult, "Fallback should return false on 500 error.");
        }

        // Now the CircuitBreaker must be OPEN. Even if the service recovers, 
        // the circuit breaker will reject requests instantly without hitting WireMock.
        stubFor(post(urlEqualTo("/api/charge"))
                .willReturn(aResponse().withStatus(200)));

        boolean afterRecoveryResult = creditCardAdapter.pay(Money.of(new BigDecimal("10.00"), "USD"));
        assertFalse(afterRecoveryResult, "CircuitBreaker is OPEN, so the request should immediately fallback and fail.");
    }
}
