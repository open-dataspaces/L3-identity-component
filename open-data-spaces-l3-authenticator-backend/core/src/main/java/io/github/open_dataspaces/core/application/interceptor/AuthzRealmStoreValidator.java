/*
 * AuthzRealmStoreValidator.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * AuthzRealmStoreValidator validates the relationship between the realm and store.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.application.interceptor;

import java.util.Map;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.enums.EnumStorePurpose;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.exception.ForbiddenException;

import io.micrometer.common.lang.NonNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * AuthzRealmStoreValidator validates the relationship between the realm and store.
 */
@Component
@Lazy
public class AuthzRealmStoreValidator implements HandlerInterceptor {

    private final APIKeyService apiKeyService;
    private final AuthorizationService authorizationService;

    /**
     * Constructor for AuthzRealmStoreValidator.
     */
    public AuthzRealmStoreValidator(APIKeyService apiKeyService, AuthorizationService authorizationService) {
        this.apiKeyService = apiKeyService;
        this.authorizationService = authorizationService;
    }

    /**
     * Validates the realm-store relationship.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param handler the handler object
     * @return true if the validation passes, false otherwise
     * @throws ForbiddenException if the realm-store validation fails
     */
    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) throws Exception {

        // Extract API key from Authorization header
        String apiKey = request.getHeader(Const.HEADER_API_KEY);
        String adminStoreId = apiKeyService.getStoreId(apiKey, EnumStorePurpose.REALM_STORE_BINDING.name());

        // Extract Path Store ID
        @SuppressWarnings("unchecked")
        Map<String, String> uriVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String pathStoreId = uriVars.get(Const.API_PATH_PARAM_STORE_ID);

        // Get the realm from API key
        String realm = apiKeyService.getIdpRealm(apiKey);

        // Evaluate authorization with explicit types
        if (!authorizationService.evaluate(
                adminStoreId,
                Const.OPENFGA_EVALUATION_SUBJECT_TYPE_REALM,
                realm,
                Const.OPENFGA_EVALUATION_RESOURCE_TYPE_STORE,
                pathStoreId,
                Const.AUTHZ_ACTION_BOUND_TO)) {
            // Authorization failed
            throw new ForbiddenException(
                    String.format(ConstError.ERRLOG_403_REALM_STORE_NOT_BOUND, realm, pathStoreId));
        }

        return true;
    }
}
