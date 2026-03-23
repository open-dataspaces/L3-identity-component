/*
 * AuthUrlController.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles user login authentication requests.
 *
 * Date: 2025/08/04
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
import io.github.open_dataspaces.core.domain.dto.AuthUrlRequest;
import io.github.open_dataspaces.core.domain.dto.AuthUrlResponse;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.ValidateException;

/**
 * Controller for handling user login authentication requests.
 *
 * <p>This controller validates the incoming request, extracts the operatorId and password.</p>
 */
@RestController
@RequestMapping(ConstPath.AUTH_PATH)
public class AuthUrlController {

    private final IdentityProviderService identityProviderService;

    /**
     * Constructor for AuthUrlController.
     *
     * @param identityProviderService the service handling user login authentication logic
     */
    public AuthUrlController(@Qualifier("keycloakService") IdentityProviderService identityProviderService) {
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
    @PostMapping(ConstPath.AUTH_URL_PATH_SHORT)
    public ResponseEntity<APIResponse<AuthUrlResponse>> url(
            @Validated @RequestBody AuthUrlRequest requestBody, BindingResult bindingResult,
            HttpServletRequest request) {
        // If failed to bind the request body, throw BadRequestException
        if (bindingResult.hasErrors()) {
            throw new ValidateException(ErrorDetails.createErrorDetails(bindingResult));
        }
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // Call the service to build the authentication URL
        String url = identityProviderService.buildAuthorizationUrl(apiKey, requestBody.getClientId(),
                requestBody.getRedirectUri(), requestBody.getCodeChallenge());

        AuthUrlResponse responseData = new AuthUrlResponse(url);

        APIResponse<AuthUrlResponse> response = new APIResponse<>(request, HttpStatus.OK.value(), responseData);

        return new ResponseEntity<APIResponse<AuthUrlResponse>>(response, HttpStatus.OK);
    }
}
