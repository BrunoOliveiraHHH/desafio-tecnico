package com.testetecnico.tarifaagua.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro de logging de requisições.
 *
 * <p>Atribui um <strong>identificador de correlação</strong> ({@code requestId})
 * a cada requisição — reaproveitando o cabeçalho {@code X-Request-Id} quando
 * presente — e o coloca no {@link MDC}, de modo que todas as linhas de log
 * geradas durante a requisição possam ser correlacionadas. Ao final, registra
 * uma linha de <em>access log</em> com método, rota, status e duração.</p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    private static final String HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestId = request.getHeader(HEADER);
        if (!StringUtils.hasText(requestId)) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }
        MDC.put(REQUEST_ID, requestId);
        response.setHeader(HEADER, requestId);

        long inicio = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long duracaoMs = (System.nanoTime() - inicio) / 1_000_000;
            log.info("{} {} -> {} ({} ms)",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duracaoMs);
            MDC.remove(REQUEST_ID);
        }
    }

    /** Não loga as chamadas de health/monitoramento (ruído). */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }
}
