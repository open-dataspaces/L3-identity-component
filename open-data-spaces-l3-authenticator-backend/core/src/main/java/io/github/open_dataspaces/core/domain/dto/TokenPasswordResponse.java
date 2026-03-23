/*
 * TokenPasswordResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Thi class is a DTO for user login responses.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.annotations.Masked;
import io.github.open_dataspaces.core.common.consts.Const;

/**
 * Data Transfer Object for user login responses.
 */
@Data
@AllArgsConstructor
public class TokenPasswordResponse {

    /**
     * The access token issued to the user.
     */
    @JsonProperty(Const.JSON_PROPERTY_ACCESS_TOKEN)
    @Masked(unmaskedPrefixLength = 10, unmaskedSuffixLength = 5)
    private String accessToken;

    /**
     * The time in seconds until the access token expires.
     */
    @JsonProperty(Const.JSON_PROPERTY_EXPIRES_IN)
    private long expiresIn;

    /**
     * The type of token issued.
     */
    @JsonProperty(Const.JSON_PROPERTY_TOKEN_TYPE)
    private String tokenType;

    /**
     * The time in seconds until the refresh token expires.
     */
    @JsonProperty(Const.JSON_PROPERTY_NOT_BEFORE_POLICY)
    private long notBeforePolicy;

    /**
     * The scope of the access token.
     */
    @JsonProperty(Const.JSON_PROPERTY_SCOPE)
    private String scope;

    /**
     * The refresh token issued to the user.
     */
    @JsonProperty(Const.JSON_PROPERTY_REFRESH_TOKEN)
    @Masked(unmaskedPrefixLength = 10, unmaskedSuffixLength = 5)
    private String refreshToken;

    /**
     * The time in seconds until the refresh token expires.
     */
    @JsonProperty(Const.JSON_PROPERTY_REFRESH_EXPIRES_IN)
    private long refreshExpiredIn;

}
