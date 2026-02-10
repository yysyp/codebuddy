package com.transaction.common.config;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Filter to inject TraceId into MDC and response headers
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TraceIdFilter implements Filter {

    private final Tracer tracer;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get or generate trace ID
        String traceId = httpRequest.getHeader("X-Trace-Id");
        if (!StringUtils.hasText(traceId)) {
            traceId = tracer.currentSpan() != null ? tracer.currentSpan().context().traceId() : generateTraceId();
        }

        // Add to response headers
        httpResponse.setHeader("X-Trace-Id", traceId);

        // Add to MDC for logging
        org.slf4j.MDC.put("traceId", traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            // Clean up MDC
            org.slf4j.MDC.remove("traceId");
        }
    }

    private String generateTraceId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}
