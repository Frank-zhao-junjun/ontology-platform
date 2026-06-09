package com.ontology.platform.api.config;

import com.ontology.platform.api.dto.RequestContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 请求上下文过滤器
 * Request Context Filter
 */
@Slf4j
@Component("apiRequestContextFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // 设置请求ID
            String requestId = httpRequest.getHeader("X-Request-ID");
            if (requestId == null || requestId.isBlank()) {
                requestId = "req_" + System.currentTimeMillis() + "_" +
                        Integer.toHexString((int) (Math.random() * 0xFFFF));
            }
            RequestContext.setRequestId(requestId);
            httpResponse.setHeader("X-Request-ID", requestId);

            // 设置用户ID
            String userId = httpRequest.getHeader("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                RequestContext.setUserId(userId);
            }

            // 记录请求开始
            long startTime = System.currentTimeMillis();
            log.debug("Request started: method={}, uri={}, requestId={}",
                    httpRequest.getMethod(), httpRequest.getRequestURI(), requestId);

            // 处理请求
            chain.doFilter(request, response);

            // 记录请求结束
            long duration = System.currentTimeMillis() - startTime;
            log.debug("Request completed: method={}, uri={}, status={}, duration={}ms, requestId={}",
                    httpRequest.getMethod(), httpRequest.getRequestURI(),
                    httpResponse.getStatus(), duration, requestId);

        } finally {
            // 清理上下文
            RequestContext.clear();
        }
    }
}
