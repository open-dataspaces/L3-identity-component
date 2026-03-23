/*
 * AuthTokenController.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles user login authentication requests.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.common.utils.ErrorDetails;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.AuthTokenRequest;
import io.github.open_dataspaces.core.domain.dto.AuthTokenResponse;
import io.github.open_dataspaces.core.domain.entities.AccessTokenResult;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.ValidateException;

/**
 * Controller for handling user login authentication requests.
 *
 * <p>This controller validates the incoming request, extracts the operatorId and password.</p>
 */
@RestController
@RequestMapping(ConstPath.AUTH_PATH)
public class AuthTokenController {

    private final IdentityProviderService identityProviderService;

    /**
     * Constructor for AuthUrlController.
     *
     * @param identityProviderService the service handling user login authentication logic
     */
    public AuthTokenController(@Qualifier("keycloakService") IdentityProviderService identityProviderService) {
        this.identityProviderService = identityProviderService;
    }

    /**
     * Handles user login authentication requests.
     *
     * @param requestBody the user login authentication request payload
     * @param bindingResult the result of validation
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the authentication response
     * @throws ValidateException if the request is invalid
     */
    @PostMapping(ConstPath.AUTH_TOKEN_PATH_SHORT)
    public ResponseEntity<APIResponse<AuthTokenResponse>> accessToken(
            @Validated @RequestBody AuthTokenRequest requestBody, BindingResult bindingResult,
            HttpServletRequest request) {
        // If failed to bind the request body, throw BadRequestException
        if (bindingResult.hasErrors()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST, ErrorDetails.createErrorDetails(bindingResult));
        }
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // Call the service to build the authentication URL
        AccessTokenResult accessTokenResult = identityProviderService.getAccessToken(apiKey,
                requestBody.getCode(), requestBody.getClientId(), requestBody.getClientSecret(),
                requestBody.getRedirectUri(), requestBody.getCodeVerifier());

        AuthTokenResponse authTokenResponse = new AuthTokenResponse(
                accessTokenResult.getAccessToken(), accessTokenResult.getExpiresIn(),
                accessTokenResult.getTokenType(), accessTokenResult.getNotBeforePolicy(),
                accessTokenResult.getScope(), accessTokenResult.getRefreshToken(),
                accessTokenResult.getRefreshExpiresIn(), accessTokenResult.getIdToken());

        APIResponse<AuthTokenResponse> response = new APIResponse<AuthTokenResponse>(request, HttpStatus.OK.value(), authTokenResponse);

        return new ResponseEntity<APIResponse<AuthTokenResponse>>(response, HttpStatus.OK);
    }
}
