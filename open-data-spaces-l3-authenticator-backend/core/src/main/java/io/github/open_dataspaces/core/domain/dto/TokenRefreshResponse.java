/*
 * TokenRefreshResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for token refresh responses.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.annotations.Masked;
import io.github.open_dataspaces.core.common.consts.Const;

import io.micrometer.common.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object for token refresh responses.
 */
@Data
@AllArgsConstructor
public class TokenRefreshResponse {

    /**
     * The access token issued for authentication.
     */
    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_ACCESS_TOKEN)
    @Masked(unmaskedPrefixLength = 10, unmaskedSuffixLength = 5)
    private String accessToken;

    /**
     * The expiration time (in seconds) for the access token.
     */
    @JsonProperty(Const.JSON_PROPERTY_EXPIRES_IN)
    private long expiresIn;

    /**
     * The type of the token (e.g., Bearer).
     */
    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_TOKEN_TYPE)
    private String tokenType;

    /**
     * The policy indicating when the token becomes valid.
     */
    @JsonProperty(Const.JSON_PROPERTY_NOT_BEFORE_POLICY)
    private int notBeforePolicy;

    /**
     * The scope of the access token.
     */
    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_SCOPE)
    private String scope;

    /**
     * The refresh token used to obtain new access tokens.
     */
    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_REFRESH_TOKEN)
    @Masked(unmaskedPrefixLength = 10, unmaskedSuffixLength = 5)
    private String refreshToken;

    /**
     * The expiration time (in seconds) for the refresh token.
     */
    @JsonProperty(Const.JSON_PROPERTY_REFRESH_EXPIRES_IN)
    private long refreshExpiresIn;

    /**
     * The ID token containing user identity information.
     * Do not output for resource owner credential flow refresh token.
     */
    @NonNull
    @JsonProperty(Const.JSON_PROPERTY_ID_TOKEN)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Masked(unmaskedPrefixLength = 10, unmaskedSuffixLength = 5)
    private String idToken;

}
