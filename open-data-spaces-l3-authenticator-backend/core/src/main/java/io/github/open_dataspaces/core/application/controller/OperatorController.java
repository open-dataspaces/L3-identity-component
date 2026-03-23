/*
 * OperatorController.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles operator information retrieval and update requests.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.controller;

import java.util.HashMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.validation.Validator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.common.utils.DateUtils;
import io.github.open_dataspaces.core.common.utils.ErrorDetails;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.IdpAccountInfo;
import io.github.open_dataspaces.core.domain.dto.ListOperatorRequest;
import io.github.open_dataspaces.core.domain.dto.GetOperatorResponse;
import io.github.open_dataspaces.core.domain.dto.OperatorResult;
import io.github.open_dataspaces.core.domain.dto.PutOperatorRequest;
import io.github.open_dataspaces.core.domain.dto.PutOperatorStatusRequest;
import io.github.open_dataspaces.core.domain.dto.StateString;
import io.github.open_dataspaces.core.domain.dto.PostOperatorRequest;
import io.github.open_dataspaces.core.domain.dto.PostOperatorResponse;
import io.github.open_dataspaces.core.domain.dto.OperatorResponse;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.domain.service.interfaces.JWTVerifyService;
import io.github.open_dataspaces.core.domain.service.interfaces.OperatorService;
import io.github.open_dataspaces.core.exception.ValidateException;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.common.config.ODSProperties;

/**
 * Controller for handling operator information retrieval and update requests.
 * This controller validates the incoming request, JWT and operator information.
 */
@RestController
@RequestMapping(ConstPath.ACCOUNT_PATH)
public class OperatorController {
    private final OperatorService operatorService;
    private final JWTVerifyService jwtService;
    private final IdentityProviderService identityProviderService;
    private final APIKeyService apiKeyService;
    private final ODSProperties odsProperties;

    @Autowired
    private Validator validator;

    /**
     * Constructor for OperatorController.
     *
     * @param operatorService the service handling operator logic
     * @param jwtService the service for JWT verification
     * @param identityProviderService the service for interacting with the identity provider
     * @param apiKeyService the service for API key operations
     * @param odsProperties the ODS application properties
     */
    public OperatorController(OperatorService operatorService, JWTVerifyService jwtService, IdentityProviderService identityProviderService, APIKeyService apiKeyService, ODSProperties odsProperties) {
        this.operatorService = operatorService;
        this.jwtService = jwtService;
        this.identityProviderService = identityProviderService;
        this.apiKeyService = apiKeyService;
        this.odsProperties = odsProperties;
    }

    /**
     * Creates a new operator.
     *
     * @param requestBody the request body containing operator details
     * @param bindingResult the result of binding the request body
     * @return ResponseEntity containing the created operator response
     */
    @PostMapping(ConstPath.ACCOUNT_OPERATOR_PATH_SHORT)
    public ResponseEntity<APIResponse<PostOperatorResponse>> postOperator(HttpServletRequest request,
            @Validated @RequestBody PostOperatorRequest requestBody, BindingResult bindingResult) {
        // Get API key from the request header
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // If failed to bind the request body, throw ValidateException
        if (bindingResult.hasErrors()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST, ErrorDetails.createErrorDetails(bindingResult));
        }

        // Get operatorId that has verified to be non-null and non-empty and is a valid UUID
        String creatorOperatorId = jwtService.getOperatorOrOpenSystemId(request);

        // Create user account in the identity provider
        IdpAccountInfo idpResult = identityProviderService.createAccount(
                apiKey,
                requestBody.getLoginUserId(),
                null,   // Email address is not used
                requestBody.getCreatePasswordFlag(),
                requestBody.getPasswordTemporaryFlag());

        // Create operator information
        OperatorResult serviceResult = null;
        try {
            if (odsProperties.isEnableUcAuthorization()) {
                // usecase store id (for authorization flows) — resolve from API key when present
                String storeId = (apiKey != null) ? apiKeyService.getUsecaseStoreId(apiKey) : null;
                serviceResult = operatorService.addOperatorWithAuth(
                    idpResult.getUserId(),
                    requestBody.getOperatorName(),
                    requestBody.getOperatorAddress(),
                    requestBody.getOpenOperatorId(),
                    requestBody.getGlobalOperatorId(),
                    DateUtils.parseDate(requestBody.getEffectiveStartDate(), Const.DATE_FORMAT),
                    DateUtils.parseDate(requestBody.getEffectiveEndDate(), Const.DATE_FORMAT),
                    creatorOperatorId,
                    storeId);
            } else {
                serviceResult = operatorService.addOperator(
                    idpResult.getUserId(),
                    requestBody.getOperatorName(),
                    requestBody.getOperatorAddress(),
                    requestBody.getOpenOperatorId(),
                    requestBody.getGlobalOperatorId(),
                    DateUtils.parseDate(requestBody.getEffectiveStartDate(), Const.DATE_FORMAT),
                    DateUtils.parseDate(requestBody.getEffectiveEndDate(), Const.DATE_FORMAT),
                    creatorOperatorId);
            }
        } catch (Exception e) {
            // If failed to create operator information, delete the user account in the identity provider
            identityProviderService.deleteAccount(apiKey, idpResult.getUserId());
            throw e;
        }

        // Prepare the response body
        PostOperatorResponse result = new PostOperatorResponse(
                serviceResult, requestBody.getLoginUserId(), idpResult.getPassword());

        // Return the response
        APIResponse<PostOperatorResponse> response = new APIResponse<>(request, HttpStatus.CREATED.value(), result);
        return new ResponseEntity<APIResponse<PostOperatorResponse>>(response, HttpStatus.CREATED);
    }

    /**
     * Updates operator information.
     *
     * @param request the HTTP servlet request
     * @param targetOperatorId the ID of the operator to update
     * @param rawJson the raw JSON request body
     * @return ResponseEntity containing the updated operator response
     * @throws IOException if there is an error reading the request body
     */
    @PutMapping(ConstPath.ACCOUNT_OPERATOR_PATH_WITH_ID_SHORT)
    @Transactional
    public ResponseEntity<APIResponse<Object>> putOperator(HttpServletRequest request,
            @PathVariable(Const.API_PATH_PARAM_OPERATOR_ID) String targetOperatorId,
            @RequestBody String rawJson // To distinguish between fields that are set to null and fields that are omitted in the request body, obtain the raw JSON.
    ) throws IOException {

        APIResponse<Object> response;

        // Validate the request body
        ObjectMapper objectMapper = new ObjectMapper();
        PutOperatorRequest requestBody = objectMapper.readValue(rawJson, PutOperatorRequest.class);

        Set<ConstraintViolation<PutOperatorRequest>> violations = validator.validate(requestBody);
        if (!violations.isEmpty()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST, ErrorDetails.createErrorDetails(violations));
        }
        requestBody.setRawJson(rawJson);

        // Get operatorId that has verified to be non-null and non-empty and is a valid UUID
        String userOperatorId = jwtService.getOperatorOrOpenSystemId(request);

        // If the operatorId in the path variable does not match the operatorId in the request body, throw IllegalAuthDataException
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // DB update
        OperatorResult serviceResult;
        if (odsProperties.isEnableUcAuthorization()) {
            String storeId = (apiKey != null) ? apiKeyService.getUsecaseStoreId(apiKey) : null;
            serviceResult = operatorService.updateOperatorWithAuth(
                    targetOperatorId,
                    DateUtils.parseDateTime(requestBody.getUpdatedAt(), Const.ISO_8601_UTC_MILLISECOND_FORMAT),
                    requestBody.getOperatorName(),
                    requestBody.getOperatorAddress(),
                    requestBody.getOpenOperatorId(),
                    requestBody.buildStateStringForProperty(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID, requestBody.getGlobalOperatorId()),
                    userOperatorId,
                    storeId);
        } else {
            serviceResult = operatorService.updateOperator(
                    targetOperatorId,
                    DateUtils.parseDateTime(requestBody.getUpdatedAt(), Const.ISO_8601_UTC_MILLISECOND_FORMAT),
                    requestBody.getOperatorName(),
                    requestBody.getOperatorAddress(),
                    requestBody.getOpenOperatorId(),
                    requestBody.buildStateStringForProperty(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID, requestBody.getGlobalOperatorId()),
                    userOperatorId);
        }

        // If the source data to update does not exist, return an empty object.
        if (serviceResult == null) {
            Object emptyOperator = new HashMap<>();
            response = new APIResponse<>(request, HttpStatus.OK.value(), emptyOperator);
        } else {
            // Keycloak update
            IdpAccountInfo idpResult = identityProviderService.updateAccount(apiKey, targetOperatorId,
                    requestBody.buildStateStringForProperty(Const.JSON_PROPERTY_LOGIN_USER_ID, requestBody.getLoginUserId()),
                    StateString.unset()); // Email address is not used

            // Prepare the response body
            OperatorResponse responseData = new OperatorResponse(serviceResult);

            // Set loginUserId and emailAddress from IdpAccountInfo
            responseData.setLoginUserId(idpResult.getLoginUserId());

            response = new APIResponse<>(request, HttpStatus.OK.value(), responseData);
        }
        // Update operator information
        return new ResponseEntity<APIResponse<Object>>(response, HttpStatus.OK);
    }

    /**
     * Updates operator information.
     *
     * @param request the HTTP servlet request
     * @param targetOperatorId the ID of the operator to update
     * @param requestBody the request body containing operator details
     * @param bindingResult the result of binding the request body
     * @return ResponseEntity containing the updated operator response
     */
    @PutMapping(ConstPath.ACCOUNT_OPERATOR_STATUS_PATH_WITH_ID_SHORT)
    @Transactional
    public ResponseEntity<APIResponse<Object>> putOperatorStatus(HttpServletRequest request,
            @PathVariable(Const.API_PATH_PARAM_OPERATOR_ID) String targetOperatorId,
            @Validated @RequestBody PutOperatorStatusRequest requestBody, BindingResult bindingResult) {

        APIResponse<Object> response;

        // Get operatorId that has verified to be non-null and non-empty and is a valid UUID
        String userOperatorId = jwtService.getOperatorOrOpenSystemId(request);

        // If failed to bind the request body, throw BadRequestException
        if (bindingResult.hasErrors()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST, ErrorDetails.createErrorDetails(bindingResult));
        }

        // If the operatorId in the path variable does not match the operatorId in the request body, throw IllegalAuthDataException
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // DB update
        OperatorResult serviceResult;
        if (odsProperties.isEnableUcAuthorization()) {
            String usecaseStoreId = (apiKey != null) ? apiKeyService.getUsecaseStoreId(apiKey) : null;
            serviceResult = operatorService.updateOperatorStatusWithAuth(
                    targetOperatorId,
                    DateUtils.parseDateTime(requestBody.getUpdatedAt(), Const.ISO_8601_UTC_MILLISECOND_FORMAT),
                    DateUtils.parseDate(requestBody.getEffectiveStartDate(), Const.DATE_FORMAT),
                    DateUtils.parseDate(requestBody.getEffectiveEndDate(), Const.DATE_FORMAT),
                    requestBody.getDeletedFlag(),
                    userOperatorId,
                    usecaseStoreId);
        } else {
            serviceResult = operatorService.updateOperatorStatus(
                    targetOperatorId,
                    DateUtils.parseDateTime(requestBody.getUpdatedAt(), Const.ISO_8601_UTC_MILLISECOND_FORMAT),
                    DateUtils.parseDate(requestBody.getEffectiveStartDate(), Const.DATE_FORMAT),
                    DateUtils.parseDate(requestBody.getEffectiveEndDate(), Const.DATE_FORMAT),
                    requestBody.getDeletedFlag(),
                    userOperatorId);
        }

        // If the source data to update does not exist, return an empty object.
        if (serviceResult == null) {
            Object emptyOperator = new HashMap<>();
            response = new APIResponse<>(request, HttpStatus.OK.value(), emptyOperator);
        } else {
            // Keycloak update
            Boolean enableFlag = (requestBody.getDeletedFlag() == null) ? null : !requestBody.getDeletedFlag();
            IdpAccountInfo idpResult = identityProviderService.updateAccountStatus(apiKey, targetOperatorId, enableFlag);

            // Prepare the response body
            OperatorResponse responseData = new OperatorResponse(serviceResult);

            // Set loginUserId and emailAddress from IdpAccountInfo
            responseData.setLoginUserId(idpResult.getLoginUserId());

            response = new APIResponse<>(request, HttpStatus.OK.value(), responseData);
        }
        // Update operator information
        return new ResponseEntity<APIResponse<Object>>(response, HttpStatus.OK);
    }

    /**
     * Retrieves operator information.
     *
     * @param request the HTTP servlet request
     * @param operatorId optional operatorId
     * @return ResponseEntity containing a list of operator responses
     */
    @GetMapping(ConstPath.ACCOUNT_OPERATOR_PATH_WITH_ID_SHORT)
    public ResponseEntity<Object> getOperator(HttpServletRequest request,
            @PathVariable(Const.API_PATH_PARAM_OPERATOR_ID) String operatorId) {
        // Get operatorId that has verified to be non-null and non-empty and is a valid UUID
        String userId = jwtService.getOperatorOrOpenSystemId(request);

        // If the operatorId in the path variable does not match the operatorId in the request body, throw IllegalAuthDataException
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // Get operator
        OperatorResult serviceResult;
        if (odsProperties.isEnableUcAuthorization()) {
            String storeId = (apiKey != null) ? apiKeyService.getUsecaseStoreId(apiKey) : null;
            serviceResult = operatorService.getOperatorWithAuth(operatorId, userId, storeId);
        } else {
            serviceResult = operatorService.getOperator(operatorId);
        }

        // If the source data to update does not exist, return an empty object.
        APIResponse<Object> responseData;
        if (serviceResult == null) {
            Object emptyOperator = new HashMap<>();
            responseData = new APIResponse<>(request, HttpStatus.OK.value(), emptyOperator);
        } else {
            GetOperatorResponse response = new GetOperatorResponse(serviceResult);
            responseData = new APIResponse<>(request, HttpStatus.OK.value(), response);
        }
        return ResponseEntity.ok(responseData);
    }

    /**
     * Lists operator information.
     *
     * @param request the HTTP servlet request
     * @param rawJson the raw JSON request body
     * @return ResponseEntity containing a list of operator responses
     * @throws IOException if there is an error reading the request body
     */
    @PostMapping(ConstPath.ACCOUNT_OPERATOR_LIST_PATH_SHORT)
    public ResponseEntity<APIResponse<List<GetOperatorResponse>>> listOperator(HttpServletRequest request,
            @RequestBody String rawJson) throws IOException {

        // Validate the request body
        ObjectMapper objectMapper = new ObjectMapper();
        ListOperatorRequest requestBody = objectMapper.readValue(rawJson, ListOperatorRequest.class);

        // If failed to bind the request body, throw ValidateException
        Set<ConstraintViolation<ListOperatorRequest>> violations = validator.validate(requestBody);
        if (!violations.isEmpty()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST, ErrorDetails.createErrorDetails(violations));
        }
        requestBody.setRawJson(rawJson);

        // Avoid null pointer exception for sort.key
        if (requestBody.getSort() == null) {
            requestBody.setSort(new ListOperatorRequest.SortKey());
        }

        // Get operatorId that has verified to be non-null and non-empty and is a valid UUID
        String userId = jwtService.getOperatorOrOpenSystemId(request);

        // If the operatorId in the path variable does not match the operatorId in the request body, throw IllegalAuthDataException
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // Get operator list
        List<OperatorResult> result;
        if (odsProperties.isEnableUcAuthorization()) {
            String storeId = (apiKey != null) ? apiKeyService.getUsecaseStoreId(apiKey) : null;
            result = operatorService.searchOperatorsWithAuth(
                    requestBody.getCount(),
                    requestBody.getIndex(),
                    requestBody.getSort().getKey(),
                    requestBody.getSort().getOrder(),
                    requestBody.getOperatorId(),
                    requestBody.getOperatorName(),
                    requestBody.getOperatorAddress(),
                    requestBody.getOpenOperatorId(),
                    requestBody.buildStateStringForProperty(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID, requestBody.getGlobalOperatorId()),
                    requestBody.getDeletedFlag(),
                    DateUtils.parseDate(requestBody.getEffectiveDate(), Const.DATE_FORMAT),
                    userId,
                    storeId
            );
        } else {
            result = operatorService.searchOperators(
                    requestBody.getCount(),
                    requestBody.getIndex(),
                    requestBody.getSort().getKey(),
                    requestBody.getSort().getOrder(),
                    requestBody.getOperatorId(),
                    requestBody.getOperatorName(),
                    requestBody.getOperatorAddress(),
                    requestBody.getOpenOperatorId(),
                    requestBody.buildStateStringForProperty(Const.JSON_PROPERTY_GLOBAL_OPERATOR_ID, requestBody.getGlobalOperatorId()),
                    requestBody.getDeletedFlag(),
                    DateUtils.parseDate(requestBody.getEffectiveDate(), Const.DATE_FORMAT)
            );
        }

        List<GetOperatorResponse> responseData = new ArrayList<>();
        for (OperatorResult operator : result) {
            responseData.add(new GetOperatorResponse(operator));
        }
        APIResponse<List<GetOperatorResponse>> response = new APIResponse<>(request, HttpStatus.OK.value(), responseData);

        return new ResponseEntity<APIResponse<List<GetOperatorResponse>>>(response, HttpStatus.OK);
    }
}
