/*
 * AccessTokenResult.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This service provides the implementation for user login logic.
 *
 * Date: 2025/08/04
 */

package io.github.open_dataspaces.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Entity representing the result of an access token request.
 */
@Data
@AllArgsConstructor
public class AccessTokenResult {

    /**
     * The access token issued for authentication.
     */
    private String accessToken;

    /**
     * The expiration time (in seconds) for the access token.
     */
    private long expiresIn;

    /**
     * The type of the token (e.g., Bearer).
     */
    private String tokenType;

    /**
     * The policy indicating when the token becomes valid.
     */
    private int notBeforePolicy;

    /**
     * The scope of the access token.
     */
    private String scope;

    /**
     * The refresh token used to obtain new access tokens.
     */
    private String refreshToken;

    /**
     * The expiration time (in seconds) for the refresh token.
     */
    private long refreshExpiresIn;

    /**
     * The ID token containing user identity information.
     */
    private String idToken;
}
