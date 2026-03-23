/*
 * APIKeyController.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles user apikey requests.
 *
 * Date: 2025/11/30
 */

package io.github.open_dataspaces.core.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.APIKeyVerifyRequest;
import io.github.open_dataspaces.core.domain.dto.APIKeyVerifyResponse;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.common.utils.ErrorDetails;
import io.github.open_dataspaces.core.exception.ValidateException;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller for handling user apikey requests.
 *
 * <p>This controller validates the incoming request and extracts the validation result related to the API key.</p>
 */
@RestController
@RequestMapping(ConstPath.AUTH_PATH)
public class APIKeyController {
    private final APIKeyService apiKeyService;

    /**
     * Constructor for APIKeyController.
     *
     * @param apiKeyService the service handling apikey logic
     */
    public APIKeyController(APIKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    /**
     * Retrieves apikey information.
     *
     * @param request the HTTP servlet request
     * @param requestBody the API key verification request  payload
     * @param bindingResult the result of validation
     * @return ResponseEntity containing a list of operator responses
     */
    @PostMapping(ConstPath.AUTH_APIKEY_VERIFY_PATH_SHORT)
    public ResponseEntity<APIResponse<APIKeyVerifyResponse>> verifyAPIKey(HttpServletRequest request,
            @Validated @RequestBody APIKeyVerifyRequest requestBody, BindingResult bindingResult) {
        // If failed to bind the request body, throw ValidateException
        if (bindingResult.hasErrors()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST, ErrorDetails.createErrorDetails(bindingResult));
        }
        boolean serviceResult = apiKeyService.verify(requestBody.getVerifyAPIKey());

        APIKeyVerifyResponse response = new APIKeyVerifyResponse(serviceResult);
        APIResponse<APIKeyVerifyResponse> apiResponse = new APIResponse<>(request, HttpStatus.OK.value(), response);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
