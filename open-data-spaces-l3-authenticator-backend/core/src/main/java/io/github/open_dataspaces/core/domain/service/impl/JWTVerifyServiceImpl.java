/*
 * JWTVerifyServiceImpl.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This service provides the implementation for JWT verification logic.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.service.impl;

import java.net.ConnectException;
import java.util.Base64;
import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.service.factory.interfaces.JwtDecoderProvider;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.domain.service.interfaces.JWTVerifyService;
import io.github.open_dataspaces.core.exception.UnauthorizedException;
import io.github.open_dataspaces.core.exception.UnexpectedException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Service implementation for JWT verification operations.
 *
 * <p>Provides logic to extract and verify claims from JWT tokens in HTTP requests.</p>
 */
@Service
public class JWTVerifyServiceImpl implements JWTVerifyService {

    // Service to interact with identity providers
    private final IdentityProviderService identityProviderService;
    // Provider for JwtDecoder instances
    private final JwtDecoderProvider jwtDecoderProvider;

    /**
     * Constructor for JWTVerifyServiceImpl.
     *
     * @param identityProviderService the service to interact with identity providers
     * @param jwtDecoderProvider the provider for JwtDecoder instances
     */
    public JWTVerifyServiceImpl(IdentityProviderService identityProviderService, JwtDecoderProvider jwtDecoderProvider) {
        this.identityProviderService = identityProviderService;
        this.jwtDecoderProvider = jwtDecoderProvider;
    }

    /**
     * Extracts and verifies the operatorId from the JWT in the HTTP request.
     *
     * @param request the HTTP servlet request containing the JWT
     * @return the operatorId extracted from the token
     */
    @Override
    public @NonNull String getOperatorOrOpenSystemId(@NonNull HttpServletRequest request) {
        String token = getToken(request);
        Map<String, Object> claimsMap = parseJwtWithoutVerification(token);

        // get operator_id
        String operatorOrOpenSystemId = (String) claimsMap.get(Const.JWT_CLAIM_OPERATOR_ID);
        if (!StringUtils.hasText(operatorOrOpenSystemId)) {
            // get open_system_id if operator_id is not present
            operatorOrOpenSystemId = (String) claimsMap.get(Const.JWT_CLAIM_OPEN_SYSTEM_ID);
        }
        if (!StringUtils.hasText(operatorOrOpenSystemId)) {
            throw new UnauthorizedException(ConstError.ERRLOG_401_INVALID_CLAIM_NO_OPERATOR_OR_OPEN_SYSTEM_ID,
                    ConstError.ERR_401_INVALID_OR_EXPIRED_TOKEN);
        }
        return operatorOrOpenSystemId;
    }

    /**
     * Parses the JWT without verification to extract claims.
     *
     * @param token the JWT token string
     * @return a map of claims extracted from the token
     */
    private @NonNull Map<String, Object> parseJwtWithoutVerification(@NonNull String token) {
        // Split the token into parts and decode the payload
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new UnauthorizedException(ConstError.ERRLOG_401_INVALID_TOKEN,
                    ConstError.ERR_401_INVALID_TOKEN);
        }
        // Decode the payload part of the JWT
        byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> claimsMap;
        try {
            claimsMap = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) { // include IOException, StreamReadException, DatabindException
            throw new UnauthorizedException(
                    String.format(ConstError.ERRLOG_401_INVALID_JWT, e.getMessage()),
                    ConstError.ERR_401_INVALID_TOKEN);
        }
        if (claimsMap == null || claimsMap.isEmpty()) {
            throw new UnauthorizedException(ConstError.ERRLOG_401_INVALID_CLAIM,
                    ConstError.ERR_401_INVALID_TOKEN);
        }
        return claimsMap;
    }

    /**
     * Extracts the token from the Authorization header of the HTTP request.
     *
     * @param request the HTTP servlet request
     * @return the token string
     */
    private @NonNull String getToken(@NonNull HttpServletRequest request) {
        // Get the Authorization header
        String authorizationHeader = request.getHeader(Const.HEADER_AUTHORIZATION);
        if (!StringUtils.hasText(authorizationHeader)) {
            throw new UnauthorizedException(ConstError.ERRLOG_401_TOKEN_EMPTY,
                    ConstError.ERR_401_TOKEN_EMPTY);
        }

        // Remove "Bearer " prefix
        String token = authorizationHeader.replaceFirst(Const.HEADER_BEARER_TOKEN_PATTERN, "");
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException(ConstError.ERRLOG_401_TOKEN_EMPTY,
                    ConstError.ERR_401_TOKEN_EMPTY);
        }
        return token;
    }

    /**
     * Verifies the JWT token from the HTTP request using the provided API key.
     */
    @Override
    public boolean verifyToken(@NonNull String apiKey, @NonNull HttpServletRequest request) {
        // Build the issuer URL using the API key
        String issuer = identityProviderService.buildIssuer(apiKey);
        // Get or create JwtDecoder for the issuer
        JwtDecoder decoder = jwtDecoderProvider.getDecoder(issuer);
        String accessToken = getToken(request);
        try {
            Jwt jwt = decoder.decode(accessToken);
            if (jwt == null) {
                throw new UnauthorizedException(ConstError.ERRLOG_401_INVALID_TOKEN,
                        ConstError.ERR_401_INVALID_OR_EXPIRED_TOKEN);
            }
            return true;
        } catch (JwtException e) {
            Throwable cause = e.getCause();
            while (cause != null) {
                // Check for connectivity issues to Keycloak
                if (cause instanceof ResourceAccessException || cause instanceof ConnectException) {
                    throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
                }
                cause = cause.getCause();
            }
            // Rethrow as UnauthorizedException for other JWT errors
            throw new UnauthorizedException(e.getMessage(),
                    ConstError.ERR_401_INVALID_OR_EXPIRED_TOKEN, e);
        }
    }
}
