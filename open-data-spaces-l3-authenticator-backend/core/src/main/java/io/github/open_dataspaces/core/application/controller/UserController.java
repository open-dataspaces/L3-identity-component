/*
 * UserController.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles personal user information retrieval and update requests.
 *
 * Date: 2025/11/30
 */

package io.github.open_dataspaces.core.application.controller;

import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.common.utils.ErrorDetails;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.IdpAccountInfo;
import io.github.open_dataspaces.core.domain.dto.PostUserRequest;
import io.github.open_dataspaces.core.domain.dto.PostUserResponse;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Controller for handling personal user information retrieval and update requests.
 * This controller validates the incoming request, JWT and personal user information.
 */
@RestController
@RequestMapping(ConstPath.ACCOUNT_PATH)
public class UserController {

    private final IdentityProviderService identityProviderService;

    @Autowired
    private Validator validator;

    /**
     * Constructor for UserController.
     *
     * @param identityProviderService the service for interacting with the identity provider
     */
    public UserController(IdentityProviderService identityProviderService) {
        this.identityProviderService = identityProviderService;
    }

    /**
     * Creates a personal user.
     *
     * @param request the HTTP servlet request
     * @param rawJson the raw JSON request body
     * @return ResponseEntity containing the created personal user response
     */
    @PostMapping(ConstPath.ACCOUNT_USER_PATH_SHORT)
    public ResponseEntity<APIResponse<PostUserResponse>> postUser(HttpServletRequest request,
            @RequestBody String rawJson     // To distinguish between fields that are set to null and fields that are omitted in the request body, obtain the raw JSON.
    ) throws IOException {
        // Get API key from the request header
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // Validate the request body
        ObjectMapper objectMapper = new ObjectMapper();
        PostUserRequest requestBody = objectMapper.readValue(rawJson, PostUserRequest.class);

        Set<ConstraintViolation<PostUserRequest>> violations = validator.validate(requestBody);
        if (!violations.isEmpty()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST, ErrorDetails.createErrorDetails(violations));
        }
        requestBody.setRawJson(rawJson);

        // Create user account in the identity provider
        IdpAccountInfo idpResult = identityProviderService.createAccount(
                apiKey,
                requestBody.getLoginUserId(),
                null,   // Email address is not used
                requestBody.getCreatePasswordFlag(),
                requestBody.getPasswordTemporaryFlag());

        // Prepare the response body
        PostUserResponse response = new PostUserResponse(idpResult);

        // Return the response
        APIResponse<PostUserResponse> apiResponse = new APIResponse<>(request, HttpStatus.CREATED.value(), response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }
}