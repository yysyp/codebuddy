package com.example.drools.interceptor;

import com.example.drools.common.response.TraceIdContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to manage traceId throughout request lifecycle
 */
@Slf4j
@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = request.getHeader(TRACE_ID_KEY);
        if (traceId == null || traceId.isEmpty()) {
            traceId = TraceIdContext.getTraceId();
        } else {
            TraceIdContext.setTraceId(traceId);
        }
        MDC.put(TRACE_ID_KEY, traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        MDC.remove(TRACE_ID_KEY);
        TraceIdContext.clearTraceId();
    }
}
