package com.ecommerce.infrastructure.web;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP-based Rate Limiting Filter using Resilience4j RateLimiter.
 * Returns HTTP 429 (Too Many Requests) when rate limit is exceeded.
 * 
 * Implements: Governor (Rate Limiting), Shed Load (Yük Reddetme), Users Antipattern
 */
@Component
public class RateLimitingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    private final RateLimiterConfig config = RateLimiterConfig.custom()
            .limitForPeriod(20)                         // 20 istek
            .limitRefreshPeriod(Duration.ofSeconds(1))  // 1 saniyede
            .timeoutDuration(Duration.ZERO)             // Bekleme yok, anında reddet
            .build();

    private final RateLimiterRegistry registry = RateLimiterRegistry.of(config);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Actuator ve auth endpoint'lerini limitleme
        String path = httpRequest.getRequestURI();
        if (path.startsWith("/actuator") || path.startsWith("/auth")) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(httpRequest);
        RateLimiter rateLimiter = limiters.computeIfAbsent(clientIp,
                ip -> registry.rateLimiter("ip-" + ip));

        if (rateLimiter.acquirePermission()) {
            chain.doFilter(request, response);
        } else {
            logger.warn("[RateLimiter] Rate limit exceeded for IP: {}, path: {}", clientIp, path);
            httpResponse.setStatus(429);
            httpResponse.setHeader("Retry-After", "1");
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                    "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Try again after 1 second.\"}"
            );
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
