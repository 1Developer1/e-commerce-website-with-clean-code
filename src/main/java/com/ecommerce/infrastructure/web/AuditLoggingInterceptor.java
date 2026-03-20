package com.ecommerce.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Audit Logging Interceptor.
 * Logs every mutating request (POST, PUT, DELETE, PATCH) with:
 * - userId (from JWT SecurityContext)
 * - HTTP method
 * - Endpoint path
 * - Response status
 * 
 * Implements: Security > Audit Logging requirement.
 */
@Component
public class AuditLoggingInterceptor implements HandlerInterceptor {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String method = request.getMethod();

        // Only audit mutating operations
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            return;
        }

        String userId = extractUserId();
        String path = request.getRequestURI();
        int status = response.getStatus();
        String clientIp = getClientIp(request);

        auditLogger.info("[AUDIT] user={} method={} path={} status={} ip={}",
                userId, method, path, status, clientIp);
    }

    private String extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getPrincipal().toString();
        }
        return "anonymous";
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
