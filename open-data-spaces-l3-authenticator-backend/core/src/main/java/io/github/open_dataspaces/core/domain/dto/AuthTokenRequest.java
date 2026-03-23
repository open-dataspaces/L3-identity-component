/*
 * AuthTokenRequest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for user login requests.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.annotations.Masked;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

/**
 * DTO representing a request for an authentication token using the IDP authorization code flow.
 *
 * <p>Contains the authorization code, client credentials, redirect URI, and PKCE code verifier.</p>
 *
 * <ul>
 *   <li><b>code</b>: Authorization code issued by the IDP.</li>
 *   <li><b>client_id</b>: Client ID registered with the IDP.</li>
 *   <li><b>client_secret</b>: Client secret associated with the client ID.</li>
 *   <li><b>redirect_uri</b>: Redirect URI used in the authorization code flow.</li>
 *   <li><b>code_verifier</b>: PKCE code verifier for enhanced security.</li>
 * </ul>
 */
@Data
public class AuthTokenRequest {

    /**
     * Authorization code for the IDP authorization code flow.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @JsonProperty(Const.JSON_PROPERTY_CODE)
    private String code;

    /**
     * Client ID for the IDP authorization code flow.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.CLIENT_ID_LENGTH_MIN, max = Const.CLIENT_ID_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_CLIENT_ID)
    private String clientId;

    /**
     * Client secret for the IDP authorization code flow.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.CLIENT_SECRET_LENGTH_MIN, max = Const.CLIENT_SECRET_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_CLIENT_SECRET)
    @Masked
    private String clientSecret;

    /**
     * Redirect URI for the IDP authorization code flow.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @Size(min = Const.REDIRECT_URI_LENGTH_MIN, max = Const.REDIRECT_URI_LENGTH_MAX, message = ConstError.ERR_VALIDATION_COMMON_SIZE)
    @JsonProperty(Const.JSON_PROPERTY_REDIRECT_URI)
    private String redirectUri;

    /**
     * PKCE code verifier for the IDP authorization code flow.
     */
    @NotBlank(message = ConstError.ERR_VALIDATION_COMMON_NOT_BLANK)
    @JsonProperty(Const.JSON_PROPERTY_CODE_VERIFIER)
    private String codeVerifier;

}
