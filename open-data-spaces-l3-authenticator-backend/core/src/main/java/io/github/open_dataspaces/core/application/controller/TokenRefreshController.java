/*
 * TokenRefreshController.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles token refresh requests.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.controller;

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
import io.github.open_dataspaces.core.domain.dto.TokenRefreshRequest;
import io.github.open_dataspaces.core.domain.dto.TokenRefreshResponse;
import io.github.open_dataspaces.core.domain.entities.AccessTokenResult;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller for handling token refresh requests.
 *
 * <p>This controller validates the incoming request, extracts refresh token.</p>
 */
@RestController
@RequestMapping(ConstPath.AUTH_PATH)
public class TokenRefreshController {

    private final IdentityProviderService identityProviderService;

    /**
     * Constructor for TokenRefreshController.
     *
     * @param identityProviderService the service handling token refresh logic
     */
    public TokenRefreshController(@Qualifier("keycloakService") IdentityProviderService identityProviderService) {
        this.identityProviderService = identityProviderService;
    }

    /**
     * Handles token refresh requests.
     *
     * @param requestBody the token refresh request payload
     * @param bindingResult the result of validation
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the refresh response
     * @throws ValidateException if the request is invalid
     */
    @PostMapping(ConstPath.AUTH_TOKEN_REFRESH_PATH_SHORT)
    public ResponseEntity<APIResponse<TokenRefreshResponse>> refresh(
            @Validated @RequestBody TokenRefreshRequest requestBody, BindingResult bindingResult,
            HttpServletRequest request) {
        // If failed to bind the request body, throw BadRequestException
        if (bindingResult.hasErrors()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST, ErrorDetails.createErrorDetails(bindingResult));
        }
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        AccessTokenResult accessTokenResult = identityProviderService.tokenRefresh(
                apiKey,
                requestBody.getRefreshToken(),
                requestBody.getClientId(),
                requestBody.getClientSecret());

        // Create the response object
        TokenRefreshResponse tokenRefreshResponse = new TokenRefreshResponse(
                accessTokenResult.getAccessToken(), accessTokenResult.getExpiresIn(),
                accessTokenResult.getTokenType(), accessTokenResult.getNotBeforePolicy(),
                accessTokenResult.getScope(), accessTokenResult.getRefreshToken(),
                accessTokenResult.getRefreshExpiresIn(), accessTokenResult.getIdToken());

        APIResponse<TokenRefreshResponse> response = new APIResponse<TokenRefreshResponse>(request, HttpStatus.OK.value(), tokenRefreshResponse);

        return new ResponseEntity<APIResponse<TokenRefreshResponse>>(response, HttpStatus.OK);
    }
}
