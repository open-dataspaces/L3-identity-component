/*
 * AbstractAPIKeyValidator.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This HandlerInterceptor provides a base interceptor for API key validation.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.interceptor;

import io.micrometer.common.lang.NonNull;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;

/**
 * Abstract interceptor for API key validation.
 *
 * <p>This class checks for the presence of an API key in the request header and delegates the actual
 * validation logic to subclasses.</p>
 */
public abstract class AbstractAPIKeyValidator implements HandlerInterceptor {

    /**
     * Checks for the presence of an API key in the request header and delegates validation.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param handler the handler object
     * @return true if the API key is valid, false otherwise
     * @throws IllegalAuthDataException if the API key is missing or invalid
     */
    @SuppressWarnings("null") // Measure against Java(67109781)
    @Override
    public final boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalAuthDataException(ConstError.ERRLOG_403_APIKEY_NOT_PROVIDED,
                    ConstError.ERR_403_APIKEY_NOT_PROVIDED);
        }

        return validateAPIKey(request, response, apiKey);
    }

    /**
     * Validates the API key.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param apiKey the API key extracted from the request header
     * @return true if the API key is valid, false otherwise
     */
    protected abstract boolean validateAPIKey(HttpServletRequest request,
            HttpServletResponse response, String apiKey);
}
