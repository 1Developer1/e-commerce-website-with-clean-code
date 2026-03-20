package com.ecommerce.payment.adapter.out.strategy;

import com.ecommerce.shared.domain.Money;
import com.ecommerce.infrastructure.tracing.TraceContextPropagator;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test Harness: Real HTTP-level chaos tests for CreditCardAdapter.
 * Uses WireMock to simulate external payment API failures at the network level.
 *
 * Covers: Dependency Failure, Latency Injection, Connection Refused, Partial Response.
 */
class CreditCardAdapterWireMockTest {

    private static WireMockServer wireMockServer;
    private CreditCardAdapter adapter;
    private Money testAmount;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();

        TraceContextPropagator tracePropagator = mock(TraceContextPropagator.class);
        when(tracePropagator.getTraceparentHeader()).thenReturn("");

        String baseUrl = "http://localhost:" + wireMockServer.port();
        adapter = new CreditCardAdapter(baseUrl, tracePropagator);
        testAmount = Money.of(new BigDecimal("100.00"), "USD");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SENARYO 1: Happy Path — API 200 OK
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    @Test
    @DisplayName("Happy Path: API 200 → pay() = true")
    void shouldReturnTrueWhenApiReturns200() {
        wireMockServer.stubFor(post(urlEqualTo("/api/charge"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"status\":\"OK\"}")));

        boolean result = adapter.pay(testAmount);

        assertTrue(result, "Normal ödeme başarılı olmalı");
        wireMockServer.verify(postRequestedFor(urlEqualTo("/api/charge")));
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SENARYO 2: Server Crash (500) → Retry → Fallback
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    @Test
    @DisplayName("Server Crash: API 500 → Retry 3x → Fallback")
    void shouldFallbackWhenApiReturns500() {
        wireMockServer.stubFor(post(urlEqualTo("/api/charge"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        boolean result = adapter.pay(testAmount);

        assertFalse(result, "500 hatası sonrası fallback (false) dönmeli");
        // Retry 3 kez denemeli → toplam 3 istek
        wireMockServer.verify(3, postRequestedFor(urlEqualTo("/api/charge")));
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SENARYO 3: Latency Injection (5s) → TimeLimiter (3s) → Timeout
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    @Test
    @DisplayName("Latency Injection: 5s delay → TimeLimiter(3s) → Fallback")
    void shouldTimeoutWhenApiIsSlow() {
        wireMockServer.stubFor(post(urlEqualTo("/api/charge"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(5000))); // 5 saniye gecikme

        long startTime = System.currentTimeMillis();
        boolean result = adapter.pay(testAmount);
        long duration = System.currentTimeMillis() - startTime;

        assertFalse(result, "Yavaş API fallback tetiklemeli");
        // TimeLimiter 3s'de keser ama Retry 3 kez dener → ~10s beklenir
        assertTrue(duration >= 8000, "Retry + TimeLimiter toplam süresi >= 8s olmalı");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SENARYO 4: Connection Refused (Port kapalı)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    @Test
    @DisplayName("Connection Refused: Kapalı port → Network Error → Fallback")
    void shouldFallbackWhenConnectionRefused() {
        TraceContextPropagator tracePropagator = mock(TraceContextPropagator.class);
        when(tracePropagator.getTraceparentHeader()).thenReturn("");

        // Kapalı olduğu bilinen bir porta çağrı yapıyoruz
        CreditCardAdapter deadAdapter = new CreditCardAdapter("http://localhost:1", tracePropagator);

        boolean result = deadAdapter.pay(testAmount);

        assertFalse(result, "Bağlantı reddedildiğinde fallback (false) dönmeli");
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SENARYO 5: Partial / Corrupt Response
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    @Test
    @DisplayName("Corrupt Response: Geçersiz body → Exception → Fallback")
    void shouldHandleCorruptResponseGracefully() {
        wireMockServer.stubFor(post(urlEqualTo("/api/charge"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFault(com.github.tomakehurst.wiremock.http.Fault.MALFORMED_RESPONSE_CHUNK)));

        boolean result = adapter.pay(testAmount);

        assertFalse(result, "Bozuk yanıt fallback tetiklemeli");
    }
}
