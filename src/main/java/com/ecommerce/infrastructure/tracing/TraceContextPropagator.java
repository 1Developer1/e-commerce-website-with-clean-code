package com.ecommerce.infrastructure.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.TraceContext;
import org.springframework.stereotype.Component;

/**
 * Utility to extract W3C traceparent header value from the current Micrometer Tracing context.
 * This lives in the Infrastructure layer and is injected into Outbound Adapters.
 */
@Component
public class TraceContextPropagator {

    private final Tracer tracer;

    public TraceContextPropagator(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Builds a W3C traceparent header value: "00-{traceId}-{spanId}-{flags}"
     * Returns empty string if no active span exists.
     */
    public String getTraceparentHeader() {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan == null) {
            return "";
        }
        TraceContext context = currentSpan.context();
        String traceId = context.traceId();
        String spanId = context.spanId();
        // "00" = version, "01" = sampled flag
        return "00-" + traceId + "-" + spanId + "-01";
    }
}
