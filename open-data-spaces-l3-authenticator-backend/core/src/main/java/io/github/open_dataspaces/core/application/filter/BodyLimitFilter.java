/*
 * BodyLimitFilter.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This filter restricts the maximum size of HTTP request bodies.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.exception.ValidateException;

/**
 * Filter for limiting the size of HTTP request bodies.
 *
 * <p>This filter checks the Content-Length of incoming requests and throws a ValidateException if the
 * request body exceeds the configured limit.</p>
 */
public class BodyLimitFilter extends OncePerRequestFilter {

    private long limit;

    /**
     * Default constructor using the default limit.
     */
    public BodyLimitFilter() {
        this.limit = Const.DEFAULT_LIMIT;
    }

    /**
     * Constructor with a custom limit.
     *
     * @param limit the maximum allowed request body size in bytes
     */
    public BodyLimitFilter(long limit) {
        this.limit = limit;
    }

    /**
     * Checks the request body size and throws an exception if it exceeds the limit.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param filterChain the filter chain
     * @throws ValidateException if the request body is too large
     * @throws UnexpectedException if an error occurs during filter processing
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) {
        if (request.getContentLengthLong() > limit) {
            throw new BadParametersException(ConstError.ERR_400_REQUEST_TOO_LARGE);
        }
        try {
            filterChain.doFilter(request, response);
        } catch (ServletException | IOException e) {
            throw new UnexpectedException(ConstError.ERR_500_FILTER_CHAIN_ERROR, e.getMessage());
        }
    }
}
