/*
 * HttpHeaderValidator.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This HandlerInterceptor provides a concrete implementation for HTTP header validation.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.interceptor;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.exception.ValidateException;

import io.micrometer.common.lang.NonNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Concrete interceptor for API key validation.
 *
 * <p>This class implements the validation logic for API keys using the APIKeyService.</p>
 */
@Component
public class HttpHeaderValidator implements HandlerInterceptor {

    /**
     * Checks for the path parameter validation.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param handler the handler object
     * @return true if the path parameter is valid, false otherwise
     * @throws ValidateException if the token is missing or invalid
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        // Validate Content-Type header
        if (request.getContentLength() > 0) {
            String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
            if (contentType == null) {
                // Content-Type header is missing
                throw new HttpMediaTypeNotSupportedException(ConstError.ERR_415_UNSUPPORTED_MEDIA_TYPE_REQUIRED);
            }
            // Parse the Content-Type header
            MediaType mediaType = MediaType.parseMediaType(contentType);
            String typeAndSubtype = String.join("/", mediaType.getType(), mediaType.getSubtype());

            // Only application/json is supported
            if (!MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(typeAndSubtype)) {
                String messageContentType = typeAndSubtype + ";charset=" + StandardCharsets.UTF_8;
                throw new HttpMediaTypeNotSupportedException(
                        String.format(ConstError.ERR_415_UNSUPPORTED_MEDIA_TYPE_NOT_SUPPORTED, messageContentType));
            }
        }

        return true;    // Continue processing the request
    }

}
