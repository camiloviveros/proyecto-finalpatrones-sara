package com.example.demo.config;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE - 1)
public class EmergencyLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(EmergencyLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        
        log.info("==> INICIO solicitud [{}] a la URL: {}", request.getMethod(), request.getRequestURI());
        log.info("==> Time: {}", new Date());
        
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("==> ERROR en solicitud [{}]: {}", request.getRequestURI(), e.getMessage(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("==> FIN solicitud [{}] a la URL: {} - Estado: {} - Duración: {}ms",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
        }
    }
}