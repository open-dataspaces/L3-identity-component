/*
 * AuthzStoreValidator.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * AuthzStoreValidator handles authorization requests by validating store IDs.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.application.interceptor;

import java.util.Map;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.enums.EnumStorePurpose;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ForbiddenException;

import io.micrometer.common.lang.NonNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * AuthzStoreValidator handles authorization requests by validating store IDs.
 */
@Component
@Lazy
public class AuthzStoreValidator implements HandlerInterceptor {
    private final APIKeyService apiKeyService;
    private final AuthorizationService authorizationService;

    /**
     * Constructor for AuthzStoreValidator.
     */
    public AuthzStoreValidator(
            APIKeyService apiKeyService,
            AuthorizationService authorizationService) {
        this.apiKeyService = apiKeyService;
        this.authorizationService = authorizationService;
    }

    /**
     * Checks store ID validity.
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
        String realmStoreId = apiKeyService.getStoreId(apiKey, EnumStorePurpose.API_EXECUTION.name());

        // Extract Path Store ID
        @SuppressWarnings("unchecked")
        Map<String, String> uriVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String pathStoreId = uriVars.get(Const.API_PATH_PARAM_STORE_ID);

        // Validate store ID
        // Check if the store exists
        ResponseEntity<Object> storeResponse = authorizationService.findStoreById(pathStoreId);
        if (!storeResponse.getStatusCode().is2xxSuccessful()) {
            throw new BadParametersException(
                    String.format(ConstError.ERRLOG_400_INVALID_REQUEST_PARAMETER, Const.API_PATH_PARAM_STORE_ID, pathStoreId));
        }

        // Reject if the Store ID linked to the Realm matches the Path Store ID
        if (realmStoreId.equals(pathStoreId)) {
            throw new ForbiddenException(String.format(ConstError.ERRLOG_403_INVALID_AUTHORIZATION, pathStoreId));
        }

        return true;
    }
}
