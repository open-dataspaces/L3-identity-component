/*
 * APIKeyValidator.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This HandlerInterceptor provides a concrete implementation for API key validation.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.interceptor;

import java.util.List;

import org.springframework.stereotype.Component;

import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.APIKeyVerifyParam;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Concrete interceptor for API key validation.
 *
 * <p>This class implements the validation logic for API keys using the APIKeyService.</p>
 */
@Component
public class APIKeyValidator extends AbstractAPIKeyValidator {

    private final APIKeyService apiKeyService;

    /**
     * Constructor for APIKeyValidator.
     *
     * @param apiKeyService the service for verifying API keys
     */
    public APIKeyValidator(APIKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    /**
     * Validates the API key.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param apiKey the API key extracted from the request header
     * @return true if the API key is valid, false otherwise
     * @throws IllegalAuthDataException if the API key is invalid
     */
    @Override
    public boolean validateAPIKey(HttpServletRequest request, HttpServletResponse response,
            String apiKey) {
        if (!apiKeyService.verify(new APIKeyVerifyParam(apiKey, null, List.of()))) {
            throw new IllegalAuthDataException(ConstError.ERRLOG_403_APIKEY_NOT_VALID,
                    ConstError.ERR_403_APIKEY_NOT_VALID);
        }
        return true;
    }
}
