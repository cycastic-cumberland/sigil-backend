package net.cycastic.sigil.controller.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class PerfFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(PerfFilter.class);
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        var start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            var end = System.currentTimeMillis();
            logger.debug("Request \"{} {}\" took {} ms. IP: {}", request.getMethod(), request.getRequestURI(), end - start, request.getRemoteAddr());
        }
    }
}
