/*
 * PlantController.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles plant information retrieval and update requests.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.common.utils.DateUtils;
import io.github.open_dataspaces.core.common.utils.ErrorDetails;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.GetPlantResponse;
import io.github.open_dataspaces.core.domain.dto.ListPlantRequest;
import io.github.open_dataspaces.core.domain.dto.PlantResponse;
import io.github.open_dataspaces.core.domain.dto.PlantResult;
import io.github.open_dataspaces.core.domain.dto.PostPlantRequest;
import io.github.open_dataspaces.core.domain.dto.PutPlantRequest;
import io.github.open_dataspaces.core.domain.dto.PutPlantStatusRequest;
import io.github.open_dataspaces.core.domain.service.interfaces.JWTVerifyService;
import io.github.open_dataspaces.core.domain.service.interfaces.PlantService;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.exception.ValidateException;
import io.github.open_dataspaces.core.common.config.ODSProperties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Controller for handling plant information retrieval and update requests.
 *
 * <p>This controller validates the incoming request, JWT and plant information.</p>
 */
@RestController
@RequestMapping(ConstPath.ACCOUNT_PATH + ConstPath.ACCOUNT_OPERATOR_PATH_SHORT)
public class PlantController {

    private final PlantService plantService;
    private final JWTVerifyService jwtService;
    private final APIKeyService apiKeyService;
    private final ODSProperties odsProperties;

    @Autowired
    private Validator validator;

    /**
     * Constructor for PlantController.
     *
     * @param plantService the service handling plant logic
     * @param jwtService the service for verifying JWT and extracting operatorId
     * @param apiKeyService the service for API key operations
     * @param odsProperties the ODS application properties
     */
    public PlantController(PlantService plantService, JWTVerifyService jwtService, APIKeyService apiKeyService, ODSProperties odsProperties) {
        this.plantService = plantService;
        this.jwtService = jwtService;
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
    @PostMapping(ConstPath.ACCOUNT_OPERATOR_PLANT_PATH_SHORT)
    public ResponseEntity<APIResponse<PlantResponse>> postPlant(HttpServletRequest request,
            @Validated @RequestBody PostPlantRequest requestBody, BindingResult bindingResult) {
        // If failed to bind the request body, throw ValidateException
        if (bindingResult.hasErrors()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST, ErrorDetails.createErrorDetails(bindingResult));
        }

        // Get operatorId that has verified to be non-null and non-empty and is a valid UUID
        String creatorOperatorId = jwtService.getOperatorOrOpenSystemId(request);
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // Create plant information
        PlantResult serviceResult;
        if (odsProperties.isEnableUcAuthorization()) {
            String storeId = (apiKey != null) ? apiKeyService.getUsecaseStoreId(apiKey) : null;
            serviceResult = plantService.addPlantWithAuth(
                    requestBody.getOperatorId(),
                    requestBody.getPlantName(),
                    requestBody.getPlantAddress(),
                    requestBody.getOpenPlantId(),
                    requestBody.getGlobalPlantId(),
                    DateUtils.parseDate(requestBody.getEffectiveStartDate(), Const.DATE_FORMAT),
                    DateUtils.parseDate(requestBody.getEffectiveEndDate(), Const.DATE_FORMAT),
                    creatorOperatorId,
                    storeId);
        } else {
            serviceResult = plantService.addPlant(
                    requestBody.getOperatorId(),
                    requestBody.getPlantName(),
                    requestBody.getPlantAddress(),
                    requestBody.getOpenPlantId(),
                    requestBody.getGlobalPlantId(),
                    DateUtils.parseDate(requestBody.getEffectiveStartDate(), Const.DATE_FORMAT),
                    DateUtils.parseDate(requestBody.getEffectiveEndDate(), Const.DATE_FORMAT),
                    creatorOperatorId);
        }

        // Prepare the response body
        PlantResponse result = new PlantResponse(serviceResult);

        // Return the response
        APIResponse<PlantResponse> response = new APIResponse<>(request, HttpStatus.CREATED.value(), result);

        return new ResponseEntity<APIResponse<PlantResponse>>(response, HttpStatus.CREATED);
    }

    /**
     * Updates plant information.
     *
     * @param request the HTTP servlet request
     * @param targetPlantId the ID of the plant to update
     * @param rawJson the raw JSON request body
     * @return ResponseEntity containing the updated plant response
     */
    @PutMapping(ConstPath.ACCOUNT_OPERATOR_PLANT_PATH_WITH_ID_SHORT)
    @Transactional
    public ResponseEntity<APIResponse<Object>> putPlant(HttpServletRequest request,
            @PathVariable(Const.API_PATH_PARAM_PLANT_ID) String targetPlantId,
            @RequestBody String rawJson     // To distinguish between fields that are set to null and fields that are omitted in the request body, obtain the raw JSON.
    ) throws IOException {

        // Validate the request body
        ObjectMapper objectMapper = new ObjectMapper();
        PutPlantRequest requestBody = objectMapper.readValue(rawJson, PutPlantRequest.class);

        Set<ConstraintViolation<PutPlantRequest>> violations = validator.validate(requestBody);
        if (!violations.isEmpty()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST, ErrorDetails.createErrorDetails(violations));
        }
        requestBody.setRawJson(rawJson);

        // Get operatorId that has verified to be non-null and non-empty and is a valid UUID
        String userOperatorId = jwtService.getOperatorOrOpenSystemId(request);
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // DB update
        PlantResult serviceResult;
        if (odsProperties.isEnableUcAuthorization()) {
            String storeId = (apiKey != null) ? apiKeyService.getUsecaseStoreId(apiKey) : null;
            serviceResult = plantService.updatePlantWithAuth(
                    targetPlantId,
                    DateUtils.parseDateTime(requestBody.getUpdatedAt(), Const.ISO_8601_UTC_MILLISECOND_FORMAT),
                    requestBody.getPlantName(),
                    requestBody.getPlantAddress(),
                    requestBody.getOpenPlantId(),
                    requestBody.buildStateStringForProperty(Const.JSON_PROPERTY_GLOBAL_PLANT_ID, requestBody.getGlobalPlantId()),
                    userOperatorId,
                    storeId);
        } else {
            serviceResult = plantService.updatePlant(
                    targetPlantId,
                    DateUtils.parseDateTime(requestBody.getUpdatedAt(), Const.ISO_8601_UTC_MILLISECOND_FORMAT),
                    requestBody.getPlantName(),
                    requestBody.getPlantAddress(),
                    requestBody.getOpenPlantId(),
                    requestBody.buildStateStringForProperty(Const.JSON_PROPERTY_GLOBAL_PLANT_ID, requestBody.getGlobalPlantId()),
                    userOperatorId);
        }

        // If the source data to update does not exist, return an empty object.
        APIResponse<Object> response;
        if (serviceResult == null) {
            Object emptyOperator = new HashMap<>();
            response = new APIResponse<>(request, HttpStatus.OK.value(), emptyOperator);
        } else {
            // Update operator information
            PlantResponse responseData = new PlantResponse(serviceResult);
            response = new APIResponse<>(request, HttpStatus.OK.value(), responseData);
        }

        return new ResponseEntity<APIResponse<Object>>(response, HttpStatus.OK);
    }

    /**
     * Updates operator information.
     *
     * @param request the HTTP servlet request
     * @param targetPlantId the ID of the plant to update
     * @param requestBody the request body containing plant details
     * @param bindingResult the result of binding the request body
     * @return ResponseEntity containing the updated plant response
     */
    @PutMapping(ConstPath.ACCOUNT_OPERATOR_PLANT_STATUS_PATH_WITH_ID_SHORT)
    @Transactional
    public ResponseEntity<APIResponse<Object>> putPlantStatus(HttpServletRequest request,
            @PathVariable(Const.API_PATH_PARAM_PLANT_ID) String targetPlantId,
            @Validated @RequestBody PutPlantStatusRequest requestBody, BindingResult bindingResult) {

        // Get operatorId that has verified to be non-null and non-empty and is a valid UUID
        String userOperatorId = jwtService.getOperatorOrOpenSystemId(request);
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // If failed to bind the request body, throw BadRequestException
        if (bindingResult.hasErrors()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST, ErrorDetails.createErrorDetails(bindingResult));
        }

        // DB update
        PlantResult serviceResult;
        if (odsProperties.isEnableUcAuthorization()) {
            String storeId = (apiKey != null) ? apiKeyService.getUsecaseStoreId(apiKey) : null;
            serviceResult = plantService.updatePlantStatusWithAuth(
                    targetPlantId,
                    DateUtils.parseDateTime(requestBody.getUpdatedAt(), Const.ISO_8601_UTC_MILLISECOND_FORMAT),
                    DateUtils.parseDate(requestBody.getEffectiveStartDate(), Const.DATE_FORMAT),
                    DateUtils.parseDate(requestBody.getEffectiveEndDate(), Const.DATE_FORMAT),
                    requestBody.getDeletedFlag(),
                    userOperatorId,
                    storeId);
        } else {
            serviceResult = plantService.updatePlantStatus(
                    targetPlantId,
                    DateUtils.parseDateTime(requestBody.getUpdatedAt(), Const.ISO_8601_UTC_MILLISECOND_FORMAT),
                    DateUtils.parseDate(requestBody.getEffectiveStartDate(), Const.DATE_FORMAT),
                    DateUtils.parseDate(requestBody.getEffectiveEndDate(), Const.DATE_FORMAT),
                    requestBody.getDeletedFlag(),
                    userOperatorId);
        }

        // If the source data to update does not exist, return an empty object.
        APIResponse<Object> response;
        if (serviceResult == null) {
            Object emptyOperator = new HashMap<>();
            response = new APIResponse<>(request, HttpStatus.OK.value(), emptyOperator);
        } else {
            // Update operator information
            PlantResponse responseData = new PlantResponse(serviceResult);
            response = new APIResponse<>(request, HttpStatus.OK.value(), responseData);
        }

        // Update operator information
        return new ResponseEntity<APIResponse<Object>>(response, HttpStatus.OK);
    }

    /**
     * Retrieves plant information by plantId.
     *
     * <p>This endpoint returns plant information for the specified plantId. If no data is found,
     * an empty object is returned in the response.</p>
     *
     * @param request the HTTP servlet request
     * @param plantId the unique identifier of the plant
     * @return ResponseEntity containing the plant information or an empty object
     */
    @GetMapping(ConstPath.ACCOUNT_OPERATOR_PLANT_PATH_WITH_ID_SHORT)
    public ResponseEntity<Object> getPlant(HttpServletRequest request,
            @PathVariable(Const.API_PATH_PARAM_PLANT_ID) String plantId) {
        // Get operatorId that has verified to be non-null and non-empty and is a valid UUID
        String userId = jwtService.getOperatorOrOpenSystemId(request);
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // Get plant
        PlantResult serviceResult;
        if (odsProperties.isEnableUcAuthorization()) {
            String storeId = (apiKey != null) ? apiKeyService.getUsecaseStoreId(apiKey) : null; // Get usecase store id
            serviceResult = plantService.getPlantWithAuth(plantId, userId, storeId);
        } else {
            serviceResult = plantService.getPlant(plantId);
        }

        // If the source data to update does not exist, return an empty object.
        APIResponse<Object> responseData;
        if (serviceResult == null) {
            Object emptyPlant = new HashMap<>();
            responseData = new APIResponse<>(request, HttpStatus.OK.value(), emptyPlant);
        } else {
            PlantResponse response = new PlantResponse(serviceResult);
            responseData = new APIResponse<>(request, HttpStatus.OK.value(), response);
        }
        return ResponseEntity.ok(responseData);
    }

    /**
     * Lists plant information.
     *
     * @param request       the HTTP servlet request
     * @param rawJson the raw JSON request body
     * @return ResponseEntity containing a list of operator responses
     * @throws IOException if there is an error reading the request body
     */
    @PostMapping(ConstPath.ACCOUNT_OPERATOR_PLANT_LIST_PATH_SHORT)
    public ResponseEntity<APIResponse<List<GetPlantResponse>>> listPlant(HttpServletRequest request,
            @RequestBody String rawJson) throws IOException {

        // Validate the request body
        ObjectMapper objectMapper = new ObjectMapper();
        ListPlantRequest requestBody = objectMapper.readValue(rawJson, ListPlantRequest.class);

        // If failed to bind the request body, throw ValidateException
        Set<ConstraintViolation<ListPlantRequest>> violations = validator.validate(requestBody);
        if (!violations.isEmpty()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST, ErrorDetails.createErrorDetails(violations));
        }
        requestBody.setRawJson(rawJson);

        // Avoid null pointer exception for sort.key
        if (requestBody.getSort() == null) {
            requestBody.setSort(new ListPlantRequest.SortKey());
        }

        // Get operatorId that has verified to be non-null and non-empty and is a valid UUID
        String userId = jwtService.getOperatorOrOpenSystemId(request);
        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // Get plant list with auth
        List<PlantResult> result;
        if (odsProperties.isEnableUcAuthorization()) {
            String storeId = (apiKey != null) ? apiKeyService.getUsecaseStoreId(apiKey) : null; // Get usecase store id
            result = plantService.searchPlantsWithAuth(
                    requestBody.getCount(),
                    requestBody.getIndex(),
                    requestBody.getSort().getKey(),
                    requestBody.getSort().getOrder(),
                    requestBody.getOperatorId(),
                    requestBody.getPlantId(),
                    requestBody.getOpenPlantId(),
                    requestBody.buildStateStringForProperty(Const.JSON_PROPERTY_GLOBAL_PLANT_ID, requestBody.getGlobalPlantId()),
                    requestBody.getPlantName(),
                    requestBody.getPlantAddress(),
                    requestBody.getDeletedFlag(),
                    DateUtils.parseDate(requestBody.getEffectiveDate(), Const.DATE_FORMAT),
                    userId,
                    storeId
            );
        } else {
            result = plantService.searchPlants(
                    requestBody.getCount(),
                    requestBody.getIndex(),
                    requestBody.getSort().getKey(),
                    requestBody.getSort().getOrder(),
                    requestBody.getOperatorId(),
                    requestBody.getPlantId(),
                    requestBody.getOpenPlantId(),
                    requestBody.buildStateStringForProperty(Const.JSON_PROPERTY_GLOBAL_PLANT_ID, requestBody.getGlobalPlantId()),
                    requestBody.getPlantName(),
                    requestBody.getPlantAddress(),
                    requestBody.getDeletedFlag(),
                    DateUtils.parseDate(requestBody.getEffectiveDate(), Const.DATE_FORMAT)
            );
        }

        List<GetPlantResponse> responseData = new ArrayList<>();
        for (PlantResult plant : result) {
            responseData.add(new GetPlantResponse(plant));
        }
        APIResponse<List<GetPlantResponse>> response = new APIResponse<>(request, HttpStatus.OK.value(), responseData);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
