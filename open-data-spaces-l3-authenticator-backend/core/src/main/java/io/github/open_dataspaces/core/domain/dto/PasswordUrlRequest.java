/*
 * PasswordUrlRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for password url requests.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object for password url requests.
 */
@Data
@AllArgsConstructor
public class PasswordUrlRequest {

    /**
     * Client ID.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.CLIENT_ID_LENGTH_MIN, max = Const.CLIENT_ID_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_CLIENT_ID)
    private String clientId;

    /**
     * Redirect URI.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.REDIRECT_URI_LENGTH_MIN, max = Const.REDIRECT_URI_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_REDIRECT_URI)
    private String redirectUri;

    /**
     * IDP Authorization Flow Code Challenge.
     * This is a code challenge used in the PKCE (Proof Key for Code Exchange) flow.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @JsonProperty(Const.JSON_PROPERTY_CODE_CHALLENGE)
    private String codeChallenge;
}
