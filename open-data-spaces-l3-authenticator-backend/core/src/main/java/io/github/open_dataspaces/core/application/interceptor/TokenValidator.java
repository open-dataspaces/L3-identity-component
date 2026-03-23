/*
 * TokenValidator.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This HandlerInterceptor provides a concrete implementation for access token validation.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.interceptor;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.domain.service.interfaces.JWTVerifyService;
import io.github.open_dataspaces.core.exception.UnauthorizedException;

import io.micrometer.common.lang.NonNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Concrete interceptor for access token validation.
 *
 * <p>This class implements the validation logic for access token using the TokeninstrospectionService.</p>
 */
@Component
@Lazy
public class TokenValidator implements HandlerInterceptor {

    private final JWTVerifyService jwtService;

    /**
     * Constructor for TokenValidator.
     *
     * @param jwtService the JWT verification service
     */
    public TokenValidator(JWTVerifyService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Checks for the access token validation.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param handler the handler object
     * @return true if the access token is valid, false otherwise
     * @throws UnauthorizedException if the token is missing or invalid
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        // get API key from header
        String apiKey = request.getHeader(Const.HEADER_API_KEY);
        // verify access token
        return jwtService.verifyToken(apiKey, request);
    }

}
