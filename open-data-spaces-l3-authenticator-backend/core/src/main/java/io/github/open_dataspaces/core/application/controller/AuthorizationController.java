/*
 * AuthorizationController.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles authorization requests by forwarding them to the PDP/PAP implementation.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.application.controller;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.domain.service.interfaces.StoreService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * AuthorizationController handles authorization requests by forwarding them to the PDP/PAP implementation.
 */
@RestController
@RequestMapping(ConstPath.AUTHORIZATION_PATH)
public class AuthorizationController {

    private final AuthorizationService authorizationService;
    private final StoreService storeService;

    /**
     * Constructor for AuthorizationController.
     */
    public AuthorizationController(AuthorizationService authorizationService, StoreService storeService) {
        this.authorizationService = authorizationService;
        this.storeService = storeService;
    }

    /**
     * Handles API gateway authorization requests.
     *
     * @param request HTTP servlet request
     * @param storeId The store ID path variable
     * @param rawJson The raw JSON request body
     * @return ResponseEntity containing the API response
     */
    @PostMapping({
        ConstPath.AUTHORIZATION_AUTHORIZATION_MODELS_PATH,
        ConstPath.AUTHORIZATION_TUPLES_READ,
        ConstPath.AUTHORIZATION_TUPLES_WRITE,
        ConstPath.AUTHORIZATION_EVALUATION,
        ConstPath.AUTHORIZATION_EVALUATIONS
    })
    public ResponseEntity<APIResponse<Object>> postApi(
            HttpServletRequest request,
            @PathVariable(Const.API_PATH_PARAM_STORE_ID) String storeId,
            @RequestBody String rawJson
    ) {
        APIResponse<Object> response;

        // Convert the request path to the forwarding path
        String forwardPath = convertForwardPath(request);

        // Forward the request to the authorization service
        ResponseEntity<Object> result = authorizationService.forward(forwardPath, HttpMethod.POST, rawJson);

        // Build the API response
        response = new APIResponse<>(request, result.getStatusCode().value(), result.getBody());
        return new ResponseEntity<APIResponse<Object>>(response, result.getStatusCode());
    }

    /**
     * Handles API gateway authorization requests.
     *
     * @param request HTTP servlet request
     * @param storeId The store ID path variable
     * @return ResponseEntity containing the API response
     */
    @GetMapping({
        ConstPath.AUTHORIZATION_STORE,
        ConstPath.AUTHORIZATION_AUTHORIZATION_MODELS_PATH
    })
    public ResponseEntity<APIResponse<Object>> getApi(
            HttpServletRequest request,
            @PathVariable(Const.API_PATH_PARAM_STORE_ID) String storeId
    ) {
        APIResponse<Object> response;

        // Convert the request path to the forwarding path
        String forwardPath = convertForwardPath(request);

        // Forward the request to the authorization service
        ResponseEntity<Object> result = authorizationService.forward(forwardPath, HttpMethod.GET, null);

        // Build the API response
        response = new APIResponse<>(request, result.getStatusCode().value(), result.getBody());
        return new ResponseEntity<APIResponse<Object>>(response, result.getStatusCode());
    }

    /**
     * Handles authorization store creation requests.
     *
     * @param request HTTP servlet request
     * @param rawJson The raw JSON request body
     * @return ResponseEntity containing the API response
     */
    @PostMapping(ConstPath.AUTHORIZATION_STORES_PATH)
    public ResponseEntity<APIResponse<Object>> postStores(
            HttpServletRequest request,
            @RequestBody String rawJson
    ) throws Exception {
        String apiKey = request.getHeader(Const.HEADER_API_KEY);
        ResponseEntity<Object> result = storeService.registerStore(apiKey, rawJson);
        APIResponse<Object> response = new APIResponse<>(request, result.getStatusCode().value(), result.getBody());
        return new ResponseEntity<APIResponse<Object>>(response, result.getStatusCode());
    }

    /**
     * Handles authorization store deletion requests.
     *
     * @param request HTTP servlet request
     * @param storeId The store ID path variable
     * @return ResponseEntity containing the API response
     */
    @DeleteMapping(ConstPath.AUTHORIZATION_STORE)
    public ResponseEntity<Object> deleteStore(
            HttpServletRequest request,
            @PathVariable(Const.API_PATH_PARAM_STORE_ID) String storeId
    ) {
        String apiKey = request.getHeader(Const.HEADER_API_KEY);
        storeService.deleteStore(apiKey, storeId);
        return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
    }

    /**
     * Converts the incoming request path to the forwarding path by removing the authorization base path.
     *
     * @param request the HTTP servlet request
     * @return the converted forwarding path
     */
    private String convertForwardPath(HttpServletRequest request) {
        // Remove the authorization base path from the request URI
        return request.getRequestURI().substring(ConstPath.AUTHORIZATION_PATH.length());
    }
}
