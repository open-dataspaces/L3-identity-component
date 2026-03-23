/*
 * AuthUrlRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for user login requests.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

/**
 * Data Transfer Object for user login requests.
 */
@Data
public class AuthUrlRequest {

    /**
     * IDP Authorization Flow Client ID.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.CLIENT_ID_LENGTH_MIN, max = Const.CLIENT_ID_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_CLIENT_ID)
    private String clientId;

    /**
     * IDP Authorization Flow Redirect URI.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.REDIRECT_URI_LENGTH_MIN, max = Const.REDIRECT_URI_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_REDIRECT_URI)
    private String redirectUri;

    /**
     * IDP Authorization Flow Code Challenge.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @JsonProperty(Const.JSON_PROPERTY_CODE_CHALLENGE)
    private String codeChallenge;
}
