/*
 * JWTVerifyService.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines the interface for JWT verification logic.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.service.interfaces;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Service interface for JWT verification operations.
 */
@Component
public interface JWTVerifyService {

    /**
     * Extracts and verifies the operatorId from the JWT in the HTTP request.
     *
     * @param request the HTTP servlet request containing the JWT
     * @return the operatorId extracted from the token
     */
    @NonNull
    String getOperatorOrOpenSystemId(@NonNull HttpServletRequest request);

    /**
     * Verifies the JWT token from the HTTP request using the provided API key.
     *
     * @param apiKey the API key associated with the token
     * @param request the HTTP servlet request containing the token
     * @return the decoded Jwt object
     */
    boolean verifyToken(@NonNull String apiKey, @NonNull HttpServletRequest request);
}
