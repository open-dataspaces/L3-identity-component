/*
 * ResponseHeaderFilter.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This filter adds common security headers to HTTP responses.
 *
 * Date: 2025/08/30
 */

package io.github.open_dataspaces.core.application.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.open_dataspaces.core.common.consts.Const;

import java.io.IOException;

/**
 * Filter to add common response headers.
 */
@Component
public class ResponseHeaderFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ResponseHeaderFilter.class);

    /**
     * Filters the HTTP request and response to add common headers.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Security headers
        response.setHeader(Const.HEADER_CONTENT_SECURITY_POLICY, Const.HEADER_CONTENT_SECURITY_POLICY_VALUE);
        response.setHeader(Const.HEADER_X_CONTENT_TYPE_OPTIONS, Const.HEADER_X_CONTENT_TYPE_OPTIONS_VALUE);
        response.setHeader(Const.HEADER_STRICT_TRANSPORT_SECURITY, Const.HEADER_STRICT_TRANSPORT_SECURITY_VALUE);

        // Tracking ID (e.g., generated with UUID)
        if (StringUtils.hasText(request.getHeader(Const.HEADER_X_TRACKING_ID))) {
            response.setHeader(Const.HEADER_X_TRACKING_ID, request.getHeader(Const.HEADER_X_TRACKING_ID));
        }

        if (LOGGER.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (String name : response.getHeaderNames()) {
                sb.append(String.format(Const.HEADER_LOG_FIELD_FORMAT, name, response.getHeaders(name)));
            }
            LOGGER.info(Const.HEADER_LOG_MESSAGE, sb.toString());
        }

        filterChain.doFilter(request, response);
    }
}
