/*
 * PasswordUrlController.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles password url requests.
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
import io.github.open_dataspaces.core.domain.dto.PasswordUrlRequest;
import io.github.open_dataspaces.core.domain.dto.PasswordUrlResponse;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.ValidateException;

/**
 * Controller for handling user password url requests.
 *
 * <p>This controller validates the incoming request, extracts the operatorId and password.</p>
 */
@RestController
@RequestMapping(ConstPath.AUTH_PATH)
public class PasswordUrlController {

    private final IdentityProviderService identityProviderService;

    /**
     * Constructor for PasswordUrlController.
     *
     * @param identityProviderService the service handling password url authentication logic
     */
    public PasswordUrlController(@Qualifier("keycloakService") IdentityProviderService identityProviderService) {
        this.identityProviderService = identityProviderService;
    }

    /**
     * Handles password url requests.
     *
     * @param requestBody password url authentication request payload
     * @param bindingResult the result of validation
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the authentication response
     * @throws ValidateException if the request is invalid
     */
    @PostMapping(ConstPath.AUTH_PASSWORD_URL_PATH_SHORT)
    public ResponseEntity<APIResponse<PasswordUrlResponse>> url(
            @Validated @RequestBody PasswordUrlRequest requestBody, BindingResult bindingResult,
            HttpServletRequest request) {
        // If failed to bind the request body, throw BadRequestException
        if (bindingResult.hasErrors()) {
            throw new ValidateException(ErrorDetails.createErrorDetails(bindingResult));
        }
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // Call the service to build the authentication URL
        String url = identityProviderService.buildPasswordChangeUrl(apiKey, requestBody.getClientId(),
                requestBody.getRedirectUri(), requestBody.getCodeChallenge());

        PasswordUrlResponse responseData = new PasswordUrlResponse(url);
        APIResponse<PasswordUrlResponse> response = new APIResponse<>(request, 200, responseData);

        return new ResponseEntity<APIResponse<PasswordUrlResponse>>(response, HttpStatus.OK);
    }
}
