/*
 * AuthzConfirmStoreIdValidator.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * AuthzConfirmStoreIdValidator handles validation of X-Confirm-Store-Id header.
 *
 * Date: 2026/2/16
 */

package io.github.open_dataspaces.core.application.interceptor;

import java.util.Map;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.exception.BadParametersException;

import io.micrometer.common.lang.NonNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * AuthzConfirmStoreIdValidator handles validation of X-Confirm-Store-Id header.
 */
@Component
@Lazy
public class AuthzConfirmStoreIdValidator implements HandlerInterceptor {
    /**
     * Checks X-Confirm-Store-Id header validity.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param handler the handler object
     * @return true if the header is valid, false otherwise
     * @throws BadParametersException if the header is missing or invalid
     */
    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) throws Exception {
        if (!HttpMethod.DELETE.name().equals(request.getMethod())) {
            return true;
        }

        // Extract Path Store ID
        @SuppressWarnings("unchecked")
        Map<String, String> uriVars = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String storeId = uriVars.get(Const.API_PATH_PARAM_STORE_ID);

        // Extract X-Confirm-Store-Id header
        String confirmStoreId = request.getHeader(Const.HEADER_X_CONFIRM_STORE_ID);
        if (confirmStoreId == null || !storeId.equals(confirmStoreId)) {
            throw new BadParametersException(
                    String.format(ConstError.ERR_400_INVALID_VALUE_OR_MISSING, Const.HEADER_X_CONFIRM_STORE_ID));
        }
        return true;
    }
}
