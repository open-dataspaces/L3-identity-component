/*
 * TokenClientController.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles client authentication requests.
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
import io.github.open_dataspaces.core.domain.dto.TokenClientRequest;
import io.github.open_dataspaces.core.domain.dto.TokenClientResponse;
import io.github.open_dataspaces.core.domain.entities.AccessTokenResult;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.ValidateException;

/**
 * Controller for handling client authentication requests.
 *
 * <p>This controller validates the incoming request, extracts the clientId and clientKey.</p>
 */
@RestController
@RequestMapping(ConstPath.AUTH_PATH)
public class TokenClientController {

    private final IdentityProviderService identityProviderService;

    /**
     * Constructor for TokenClientController.
     *
     * @param identityProviderService the identity provider service for handling client authentication
     */
    public TokenClientController(@Qualifier("keycloakService") IdentityProviderService identityProviderService) {
        this.identityProviderService = identityProviderService;
    }

    /**
     * Handles client authentication requests.
     *
     * @param requestBody the client authentication request payload
     * @param bindingResult the result of validation
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the authentication response
     * @throws ValidateException if the request is invalid
     */
    @PostMapping(ConstPath.AUTH_TOKEN_CLIENT_SHORT)
    public ResponseEntity<APIResponse<TokenClientResponse>> client(
            @Validated @RequestBody TokenClientRequest requestBody, BindingResult bindingResult,
            HttpServletRequest request) {
        // If failed to bind the request body, throw BadRequestException
        if (bindingResult.hasErrors()) {
            throw new ValidateException(ErrorDetails.createErrorDetails(bindingResult));
        }

        // Attempt client login
        String apiKey = request.getHeader(Const.HEADER_API_KEY);
        AccessTokenResult result = identityProviderService.signInWithClient(apiKey, requestBody.getClientId(), requestBody.getClientSecret());

        // Build the response
        TokenClientResponse responseData = new TokenClientResponse(result.getAccessToken(), result.getExpiresIn(), result.getTokenType(),
                result.getNotBeforePolicy(), result.getScope());
        APIResponse<TokenClientResponse> response = new APIResponse<>(request, HttpStatus.OK.value(), responseData);
        return new ResponseEntity<APIResponse<TokenClientResponse>>(response, HttpStatus.OK);
    }
}
