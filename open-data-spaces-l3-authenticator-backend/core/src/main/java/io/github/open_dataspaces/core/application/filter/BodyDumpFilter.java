/*
 * BodyDumpFilter.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This file is part of the user authentication backend system.
 *
 * BodyDumpFilter is a servlet filter that logs request and response bodies
 * for debugging and auditing purposes. The output is controlled by the environment
 *
 * @author btmurayamakohei
 * @since 2025/06/11
 */

package io.github.open_dataspaces.core.application.filter;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import io.github.open_dataspaces.core.common.consts.Const;

import org.springframework.lang.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter that logs request and response bodies for debugging and auditing purposes.
 *
 * <p>This filter captures and logs HTTP request and response bodies for each API call,
 * which is useful for debugging and auditing. The output is controlled by the environment
 * </p>
 *
 * <p>The filter also sets the operatorId header in the response if available.
 * </p>
 *
 * @author btmurayamakohei
 * @since 2025/06/11
 */
public class BodyDumpFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BodyDumpFilter.class);

    /**
     * Handles the logging of request and response bodies.
     *
     * @param request      the HTTP request
     * @param response     the HTTP response
     * @param filterChain  the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Capture request and response bodies
        ContentCachingRequestWrapper wrappedRequest =
                (request instanceof ContentCachingRequestWrapper)
                ? (ContentCachingRequestWrapper) request : new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse =
                (response instanceof ContentCachingResponseWrapper)
                ? (ContentCachingResponseWrapper) response : new ContentCachingResponseWrapper(response);
        // Pass to the next filter
        filterChain.doFilter(wrappedRequest, wrappedResponse);
        //  If the request body is empty, read it to ensure it is cached
        if (wrappedRequest.getContentAsByteArray().length == 0) {
            wrappedRequest.getInputStream().readAllBytes();
        }

        // Log output for debugging
        if (LOGGER.isDebugEnabled()) {
            // Get request and response bodies
            String reqBody = new String(wrappedRequest.getContentAsByteArray(), wrappedRequest.getCharacterEncoding())
                    .replaceAll("[\\n\\r]+", "");
            String resBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8)
                    .replaceAll("[\\n\\r]+", "");
            // Log the request and response bodies
            LOGGER.debug(String.format(Const.DUMP_BODY,
                    request.getRequestURI(),
                    getHeadeString(request, true),
                    reqBody,
                    resBody)
            );
        }
        // Log output for auditing
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format(Const.DUMP_BODY,
                    request.getRequestURI(),
                    getHeadeString(request, false),
                    Const.MASK,
                    Const.MASK)
            );
        }

        // Copy the request and response bodies back to the response
        // When you call wrappedResponse.copyBodyToResponse(), the response body is written out and the internal buffer is cleared.
        // As a result, subsequent filters (such as AuthDumpFilter) cannot access the response body anymore.
        wrappedResponse.copyBodyToResponse();
    }

    /**
     * Generates a string representation of the request headers.
     *
     * @param request the HTTP request
     * @param isDebugEnabled whether debug logging is enabled
     * @return a string containing the headers
     */
    private String getHeadeString(HttpServletRequest request, boolean isDebugEnabled) {
        StringBuilder headersBuilder = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (isDebugEnabled || !headerName.equalsIgnoreCase(Const.HEADER_AUTHORIZATION)) {
                headersBuilder.append(headerName).append(": ")
                    .append(request.getHeader(headerName)).append("; ");
            } else {
                // Mask the Authorization header for security
                headersBuilder.append(headerName).append(": ").append(Const.MASK).append("; ");
            }
        }
        return headersBuilder.toString();
    }
}
