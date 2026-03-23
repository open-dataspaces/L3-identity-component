/*
 * TokenPasswordController.java
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
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.common.utils.ErrorDetails;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.TokenPasswordRequest;
import io.github.open_dataspaces.core.domain.dto.TokenPasswordResponse;
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
public class TokenPasswordController {

    private final IdentityProviderService identityProviderService;

    /**
     * Constructor for TokenPasswordController.
     *
     * @param identityProviderService the identity provider service for handling user authentication
     */
    public TokenPasswordController(@Qualifier("keycloakService") IdentityProviderService identityProviderService) {
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
    @PostMapping(ConstPath.AUTH_TOKEN_PASSWORD_SHORT)
    public ResponseEntity<APIResponse<TokenPasswordResponse>> login(
            @Validated @RequestBody TokenPasswordRequest requestBody, BindingResult bindingResult,
            HttpServletRequest request) {
        // If failed to bind the request body, throw BadRequestException
        if (bindingResult.hasErrors()) {
            throw new ValidateException(ErrorDetails.createErrorDetails(bindingResult));
        }

        // Attemp to login
        String apiKey = request.getHeader(Const.HEADER_API_KEY);
        AccessTokenResult result = identityProviderService.signInWithPassword(apiKey, requestBody.getClientId(), requestBody.getClientSecret(),
                requestBody.getLoginUserId(), requestBody.getPassword());

        // Build the response
        TokenPasswordResponse responseData = new TokenPasswordResponse(result.getAccessToken(), result.getExpiresIn(), result.getTokenType(),
                result.getNotBeforePolicy(), result.getScope(), result.getRefreshToken(), result.getRefreshExpiresIn());
        APIResponse<TokenPasswordResponse> response = new APIResponse<>(request, HttpStatus.OK.value(), responseData);
        return new ResponseEntity<APIResponse<TokenPasswordResponse>>(response, HttpStatus.OK);
    }
}
