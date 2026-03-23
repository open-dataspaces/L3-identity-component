/*
 * TokenIntrospectionController.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles token introspection requests.
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
import io.github.open_dataspaces.core.domain.dto.TokenIntrospectionRequest;
import io.github.open_dataspaces.core.domain.dto.TokenIntrospectionResponse;
import io.github.open_dataspaces.core.domain.entities.TokenIntrospectionResult;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.ValidateException;

/**
 * Controller for handling token introspection requests.
 *
 * <p>This controller provides an endpoint for validating and introspecting tokens.</p>
 */
@RestController
@RequestMapping(ConstPath.AUTH_PATH)
public class TokenIntrospectionController {

    private final IdentityProviderService identityProviderService;

    /**
     * Constructor for TokenIntrospectionController.
     *
     * @param identityProviderService the service handling identity provider logic
     */
    public TokenIntrospectionController(@Qualifier("keycloakService") IdentityProviderService identityProviderService) {
        this.identityProviderService = identityProviderService;
    }

    /**
     * Handles token introspection requests.
     *
     * @param requestBody the token introspection request payload
     * @param bindingResult the result of validation
     * @param request the HTTP servlet request
     * @return ResponseEntity containing the introspection response
     * @throws ValidateException if the request is invalid
     */
    @PostMapping(ConstPath.AUTH_TOKEN_INTROSPECT_PATH_SHORT)
    public ResponseEntity<APIResponse<TokenIntrospectionResponse>> tokenIntrospection(
            @Validated @RequestBody TokenIntrospectionRequest requestBody,
            BindingResult bindingResult, HttpServletRequest request) {
        // If failed to bind the request body, throw BadRequestException
        if (bindingResult.hasErrors()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST, ErrorDetails.createErrorDetails(bindingResult));
        }
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // Call the service to introspect the token
        TokenIntrospectionResult tokenIntrospectResult = identityProviderService.tokenIntrospection(
                apiKey, requestBody.getAccessToken(), requestBody.getClientId(), requestBody.getClientSecret());

        TokenIntrospectionResponse responseData = null;
        if (tokenIntrospectResult.isActive()) {
            // If the access token is valid, create the response data
            responseData = new TokenIntrospectionResponse(
                tokenIntrospectResult.getExp(),
                tokenIntrospectResult.getIat(),
                tokenIntrospectResult.getOperatorId(),
                tokenIntrospectResult.getOpenSystemId(), // No operator account ID in introspection response
                tokenIntrospectResult.getScope(),
                tokenIntrospectResult.getClientId(),
                tokenIntrospectResult.getTyp()
            );
        } else {
            // If the access token is invalid
            responseData = new TokenIntrospectionResponse();
        }

        APIResponse<TokenIntrospectionResponse> response = new APIResponse<>(request, HttpStatus.OK.value(), responseData);

        return new ResponseEntity<APIResponse<TokenIntrospectionResponse>>(response, HttpStatus.OK);
    }
}
