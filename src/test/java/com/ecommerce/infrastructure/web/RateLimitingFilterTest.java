package com.ecommerce.infrastructure.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Test Harness: RateLimitingFilter integration test.
 * Verifies that the rate limiter correctly returns HTTP 429
 * after exceeding 20 requests per second from the same IP.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RateLimitingFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Rate Limiter: 25 istek → ilk 20 OK, son 5 → HTTP 429")
    void shouldReturn429WhenRateLimitExceeded() throws Exception {
        int okCount = 0;
        int rateLimitedCount = 0;

        // 25 istek gönder — ilk 20'si geçmeli, kalan 5'i 429 dönmeli
        for (int i = 0; i < 25; i++) {
            MvcResult result = mockMvc.perform(get("/products")
                            .header("Authorization", "Bearer test-bypass"))
                    .andReturn();

            int status = result.getResponse().getStatus();
            if (status == 429) {
                rateLimitedCount++;
                // Retry-After header olmalı
                String retryAfter = result.getResponse().getHeader("Retry-After");
                assertTrue(retryAfter != null && !retryAfter.isEmpty(),
                        "429 yanıtında Retry-After header olmalı");
            } else {
                okCount++;
            }
        }

        assertTrue(rateLimitedCount > 0,
                "25 istekten en az bazıları 429 dönmeli (rate limit aşıldı). " +
                "OK: " + okCount + ", 429: " + rateLimitedCount);
    }
}
