/*
 * AuthDumpFilter.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This file is part of the user authentication backend system.
 *
 * AuthDumpFilter is a servlet filter that logs authentication-related request and response bodies
 * for debugging and auditing purposes. Sensitive fields such as passwords and tokens are masked before logging.
 *
 * @author btmurayamakohei
 * @since 2025/06/11
 */

package io.github.open_dataspaces.core.application.filter;

import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.open_dataspaces.core.application.filter.model.AuthDumpInfo;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.utils.MaskingUtils;

/**
 * Filter that logs authentication request and response bodies for debugging and auditing purposes.
 *
 * <p>This filter captures and logs HTTP request and response bodies for each authentication-related API call.
 * Sensitive fields such as passwords and tokens are masked before logging.
 * The log output includes event type, request/response bodies, timestamp, IP address, and API key.
 * </p>
 *
 * <p>The filter branches its logging logic depending on the API path (e.g., token, login, refresh, change password).
 * </p>
 *
 * @author btmurayamakohei
 * @since 2025/06/11
 */
public class AuthDumpFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthDumpFilter.class);

    /**
     * Intercepts HTTP requests and responses, captures their bodies, and delegates logging to the appropriate handler
     * based on the API path.
     *
     * @param request      the HTTP request
     * @param response     the HTTP response
     * @param filterChain  the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
        throws ServletException, IOException {

        // Capture request and response body
        ContentCachingRequestWrapper wrappedRequest =
                (request instanceof ContentCachingRequestWrapper)
                ? (ContentCachingRequestWrapper) request : new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse =
                (response instanceof ContentCachingResponseWrapper)
                ? (ContentCachingResponseWrapper) response : new ContentCachingResponseWrapper(response);

        // Proceed with the filter chain
        filterChain.doFilter(wrappedRequest, wrappedResponse);

        //  If the request body is empty, read it to ensure it is cached
        if (wrappedRequest.getContentAsByteArray().length == 0) {
            wrappedRequest.getInputStream().readAllBytes();
        }

        // Get request and response bodies
        String reqBody = new String(wrappedRequest.getContentAsByteArray(), wrappedRequest.getCharacterEncoding());
        String resBody = new String(wrappedResponse.getContentAsByteArray(), wrappedResponse.getCharacterEncoding());

        // Mask sensitive fields in request and response bodies
        reqBody = MaskingUtils.maskJsonFieldsByAnnotation(reqBody);
        resBody = MaskingUtils.maskJsonFieldsByAnnotation(resBody);

        // If maskJsonField fails, return.
        authDump(request, reqBody, resBody, (wrappedResponse.getStatus() == HttpServletResponse.SC_CREATED));

        // Copy response body back to response
        wrappedResponse.copyBodyToResponse();
    }

    /**
     * Logs authentication dump information.
     *
     * @param request the HTTP request
     * @param req the request body
     * @param res the response body
     * @param event the event type (e.g., token, login)
     * @param isRequestResult whether the request was successful
     */
    private void authDump(HttpServletRequest request, String req, String res, boolean isRequestResult) {
        String timeStamp = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(Const.ISO_8601_UTC_FORMAT));
        String event = request.getRequestURI();
        String ipAddress = request.getRemoteAddr();
        String apiKey = request.getHeader(Const.HEADER_API_KEY);
        // Create dump information object
        AuthDumpInfo dumpInfo = new AuthDumpInfo(event, isRequestResult, req, res, timeStamp, ipAddress, apiKey);
        // Convert to JSON string and output to log
        try {
            if (LOGGER.isInfoEnabled()) {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(dumpInfo);
                LOGGER.info(json);
            }
        } catch (JsonProcessingException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(String.format(ConstError.ERRLOG_DUMP_SERIALIZE_FAILED, e.getMessage()));
            }
        }
    }
}
