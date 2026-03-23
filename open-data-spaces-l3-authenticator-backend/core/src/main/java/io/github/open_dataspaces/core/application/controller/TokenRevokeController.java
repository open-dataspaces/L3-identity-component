/*
 * TokenRevokeController.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles token revoke requests.
 *
 * Date: 2026/02/10
 */

package io.github.open_dataspaces.core.application.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.common.utils.ErrorDetails;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.TokenRevokeRequest;
import io.github.open_dataspaces.core.domain.dto.TokenRevokeResponse;
import io.github.open_dataspaces.core.domain.entities.TokenRevokeResult;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller for handling token revoke operations.
 */
@RestController
@RequestMapping(ConstPath.AUTH_PATH)
public class TokenRevokeController {
    private final IdentityProviderService identityProviderService;

    /**
     * Constructor for TokenRevokeController.
     *
     * @param identityProviderService the service handling token revoke logic
     */
    public TokenRevokeController(@Qualifier("keycloakService") IdentityProviderService identityProviderService) {
        this.identityProviderService = identityProviderService;
    }

    /**
     * Handles POST requests to revoke tokens.
     *
     * @param requestBody the token revoke request body
     * @param bindingResult the result of validation
     * @param request the HTTP servlet request
     * @return the response entity containing the API response
     * @throws ValidateException if the request body is invalid
     */
    @PostMapping(ConstPath.AUTH_TOKEN_REVOKE_PATH_SHORT)
    public ResponseEntity<APIResponse<TokenRevokeResponse>> revoke(@Validated @RequestBody TokenRevokeRequest requestBody, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST, ErrorDetails.createErrorDetails(bindingResult));
        }
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        TokenRevokeResult tokenRevokeResult = identityProviderService.revoke(apiKey, requestBody.getClientId(), requestBody.getClientSecret(), requestBody.getRefreshToken());

        TokenRevokeResponse tokenRevokeResponse = new TokenRevokeResponse(tokenRevokeResult.isActive());

        APIResponse<TokenRevokeResponse> apiResponse = new APIResponse<TokenRevokeResponse>(request, HttpStatus.OK.value(), tokenRevokeResponse);

        return new ResponseEntity<APIResponse<TokenRevokeResponse>>(apiResponse, HttpStatus.OK);
    }

}
