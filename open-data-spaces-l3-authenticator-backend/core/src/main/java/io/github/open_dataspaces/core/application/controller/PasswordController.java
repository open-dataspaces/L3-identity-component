/*
 * PasswordController.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles password change requests for authenticated users. It validates the
 * request body, verifies the operatorId using JWT, and delegates the password change operation to
 * the service layer.
 *
 * Date: 2025-06-30
 */

package io.github.open_dataspaces.core.application.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import io.github.open_dataspaces.core.domain.dto.PasswordRequest;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.ValidateException;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.common.utils.ErrorDetails;

/**
 * Controller for handling password change requests.
 *
 * <p>This controller validates the incoming request, extracts the operatorId from the JWT, and
 * delegates the password change operation to the service layer.</p>
 */
@RestController
@RequestMapping(ConstPath.AUTH_PATH)
public class PasswordController {

    private final IdentityProviderService identityProviderService;

    /**
     * Constructor for AuthChangeController.
     *
     * @param identityProviderService the identity provider service for handling password changes
     */
    public PasswordController(@Qualifier("keycloakService") IdentityProviderService identityProviderService) {
        this.identityProviderService = identityProviderService;
    }

    /**
     * Handles password change requests.
     *
     * @param requestBody the request body containing the user ID and new password
     * @param bindingResult the result of validating the request body
     * @param request the HTTP servlet request containing the JWT token
     * @return a ResponseEntity indicating the result of the operation
     * @throws ValidateException if the request body validation fails
     */
    @PutMapping(ConstPath.AUTH_PASSWORD_SHORT)
    public ResponseEntity<Object> changePassword(
            @Validated @RequestBody PasswordRequest requestBody, BindingResult bindingResult,
            HttpServletRequest request, @PathVariable(Const.API_PATH_PARAM_OPERATOR_ID) String operatorId) {
        // If failed to bind the request body, throw BadRequestException
        if (bindingResult.hasErrors()) {
            throw new ValidateException(ErrorDetails.createErrorDetails(bindingResult));
        }

        // Attempt change password
        String apiKey = request.getHeader(Const.HEADER_API_KEY);
        identityProviderService.changePassword(apiKey, operatorId, requestBody.getOldPassword(),
                requestBody.getNewPassword());

        return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
    }
}
