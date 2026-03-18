package com.transaction.tagging.controlpanel.config;

import com.transaction.tagging.common.util.TraceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter for setting up trace context for each request.
 * Adds traceId to logging MDC and API responses.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                     FilterChain filterChain) throws ServletException, IOException {
        // Get or generate trace ID
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = TraceContext.generateTraceId();
        }
        
        // Set trace ID in context and MDC
        TraceContext.setTraceId(traceId);
        MDC.put(TRACE_ID_MDC_KEY, traceId);
        
        // Add trace ID to response header
        response.setHeader(TRACE_ID_HEADER, traceId);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clean up
            TraceContext.clear();
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }
}
