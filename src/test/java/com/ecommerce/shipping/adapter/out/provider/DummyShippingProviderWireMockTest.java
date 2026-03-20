package com.ecommerce.shipping.adapter.out.provider;

import com.ecommerce.infrastructure.tracing.TraceContextPropagator;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test Harness: Real HTTP-level chaos tests for DummyShippingProvider.
 * Uses WireMock to simulate external shipping API failures.
 */
class DummyShippingProviderWireMockTest {

    private static WireMockServer wireMockServer;
    private DummyShippingProvider provider;

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
        provider = new DummyShippingProvider(baseUrl, tracePropagator);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SENARYO 1: Happy Path — API 200 OK
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    @Test
    @DisplayName("Happy Path: API 200 → Valid tracking code")
    void shouldReturnTrackingCodeWhenApiReturns200() {
        wireMockServer.stubFor(post(urlEqualTo("/api/shipment"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"trackingCode\":\"TRK-12345678\"}")));

        String trackingCode = provider.generateTrackingCode("Istanbul, Turkey");

        assertNotNull(trackingCode, "Tracking code null olmamalı");
        assertTrue(trackingCode.startsWith("TRK-"), "Tracking code TRK- ile başlamalı");
        assertFalse(trackingCode.contains("FALLBACK"), "Normal akışta FALLBACK olmamalı");
        wireMockServer.verify(postRequestedFor(urlEqualTo("/api/shipment")));
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SENARYO 2: Server Crash (500) → CircuitBreaker → Fallback
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    @Test
    @DisplayName("Server Crash: API 500 → Retry 3x → Fallback tracking code")
    void shouldReturnFallbackTrackingWhenApiCrashes() {
        wireMockServer.stubFor(post(urlEqualTo("/api/shipment"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        String trackingCode = provider.generateTrackingCode("Ankara, Turkey");

        assertNotNull(trackingCode, "Tracking code null olmamalı (fallback dönmeli)");
        assertEquals("TRK-FALLBACK-PENDING", trackingCode,
                "Server çöktüğünde fallback tracking code dönmeli");
        // Retry 3 kez denemiş olmalı
        wireMockServer.verify(3, postRequestedFor(urlEqualTo("/api/shipment")));
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SENARYO 3: Slow Response (3s) → TimeLimiter (2s) → Timeout
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    @Test
    @DisplayName("Slow Response: 3s delay → TimeLimiter(2s) → Fallback")
    void shouldTimeoutWhenApiIsSlow() {
        wireMockServer.stubFor(post(urlEqualTo("/api/shipment"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(3000))); // 3 saniye gecikme, TimeLimiter 2s

        long startTime = System.currentTimeMillis();
        String trackingCode = provider.generateTrackingCode("Izmir, Turkey");
        long duration = System.currentTimeMillis() - startTime;

        assertEquals("TRK-FALLBACK-PENDING", trackingCode,
                "Yavaş API fallback tracking code üretmeli");
        // Retry 3 kez dener, her biri 2s'de kesilir → ~7s+ beklenir
        assertTrue(duration >= 5000, "Retry + TimeLimiter toplam süresi >= 5s olmalı");
    }
}
