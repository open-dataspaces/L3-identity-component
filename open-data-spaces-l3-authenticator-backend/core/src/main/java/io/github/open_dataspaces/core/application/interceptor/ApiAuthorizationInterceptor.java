/*
 * ApiAuthorizationInterceptor.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles authorization requests by forwarding them to the PDP/PAP implementation.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.application.interceptor;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.enums.EnumStorePurpose;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.domain.service.interfaces.JWTVerifyService;
import io.github.open_dataspaces.core.exception.ForbiddenException;

import io.micrometer.common.lang.NonNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ApiAuthorizationInterceptor handles authorization requests by forwarding them to the PDP/PAP implementation.
 */
@Component
@Lazy
public class ApiAuthorizationInterceptor implements HandlerInterceptor {

    private final JWTVerifyService jwtService;
    private final APIKeyService apiKeyService;

    private final AuthorizationService authorizationService;

    /**
     * Constructor for AuthorizationInterceptor.
     */
    public ApiAuthorizationInterceptor(
            JWTVerifyService jwtService,
            APIKeyService apiKeyService,
            AuthorizationService authorizationService) {
        this.jwtService = jwtService;
        this.apiKeyService = apiKeyService;
        this.authorizationService = authorizationService;
    }

    /**
     * Checks authorization for the API request.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param handler the handler object
     * @return true if the access token is valid, false otherwise
     * @throws ForbiddenException if the token is missing or invalid
     */
    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) throws Exception {

        // Extract token from Authorization header
        String apiKey = request.getHeader(Const.HEADER_API_KEY);
        String storeId = apiKeyService.getStoreId(apiKey, EnumStorePurpose.API_EXECUTION.name());

        String userId = jwtService.getOperatorOrOpenSystemId(request);  // User is the operator ID or open system ID
        String resource = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);      // Resource is the matched URI pattern
        String action = request.getMethod();    // Action is the HTTP method

        // Evaluate authorization
        if (!authorizationService.evaluate(storeId, userId, resource, action)) {
            // Authorization failed
            throw new ForbiddenException(
                    String.format(ConstError.ERR_403_AUTHORIZATION_FAILED, userId, resource, action));
        }

        return true;
    }
}
