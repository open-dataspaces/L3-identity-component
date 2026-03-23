/*
 * OpenFgaServiceImpl.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Implementation of the OpenFgaService interface for forwarding requests to the OpenFGA service.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.infrastructure.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.common.utils.ErrorDetails;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.NotFoundException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.infrastructure.config.OpenFgaProperties;
import io.github.open_dataspaces.core.infrastructure.dto.EvaluateRequest;
import io.github.open_dataspaces.core.infrastructure.dto.EvaluateResponse;
import io.github.open_dataspaces.core.infrastructure.dto.EvaluationsRequest;
import io.github.open_dataspaces.core.infrastructure.dto.EvaluationsResponse;
import io.github.open_dataspaces.core.infrastructure.dto.EvaluationsResponse.EvaluationDecision;
import io.github.open_dataspaces.core.infrastructure.dto.Tuple;
import io.github.open_dataspaces.core.infrastructure.dto.TupleDeleteRequest;
import io.github.open_dataspaces.core.infrastructure.dto.TupleDeleteRequest.Deletes;
import io.github.open_dataspaces.core.infrastructure.dto.TupleWriteRequest;
import io.github.open_dataspaces.core.infrastructure.dto.TupleWriteRequest.Writes;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Implementation of the AuthorizationService interface for forwarding requests to the OpenFGA service.
 */
@Service("OpenFgaService")
public class OpenFgaServiceImpl implements AuthorizationService {

    private final OpenFgaProperties openFgaProperties;
    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    private final Validator validator;

    /**
     * Constructor for OpenFgaServiceImpl.
     */
    public OpenFgaServiceImpl(
            OpenFgaProperties openFgaProperties,
            ObjectMapper objectMapper,
            @Qualifier("openFgaRestTemplate") RestTemplate restTemplate,
            Validator validator) {
        this.openFgaProperties = openFgaProperties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.validator = validator;
    }

    /**
     * Forwards a JSON request to the OpenFGA service.
     */
    @Override
    public ResponseEntity<Object> forward(
            String apiEndPointPath,
            HttpMethod httpMethod,
            String payload
    ) {
        // Construct the full URL
        String url = String.join("", openFgaProperties.getApiEndpoint(), apiEndPointPath);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(payload)) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        headers.setBearerAuth(openFgaProperties.getPresharedKey()); // Set the preshared key as Bearer token
        // Create the request entity
        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        // Forward the request and return the response
        ResponseEntity<String> openFgaResponseEntity;
        try {
            openFgaResponseEntity = restTemplate.exchange(url, httpMethod, request, String.class);
        } catch (RestClientException e) {
            // Handle network/communication errors
            throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
        }
        // Check for HTTP error status codes and throw exceptions if needed
        throwIfErrorStatus(openFgaResponseEntity);

        // Parse the response body
        String bodyJsonString = openFgaResponseEntity.getBody();
        Object parsed = null;
        if (StringUtils.hasText(bodyJsonString)) {
            try {
                parsed = objectMapper.readValue(bodyJsonString, Object.class);
            } catch (Exception e) {
                throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
            }
        }
        // Return the response entity with parsed body
        return new ResponseEntity<>(parsed, openFgaResponseEntity.getStatusCode());
    }

    /**
     * Deletes a store in the authorization service.
     */
    @Override
    public ResponseEntity<Object> deleteStore(String storeId) {
        String apiEndPointPath = String.join("/", ConstPath.AUTHORIZATION_STORES_PATH, storeId);
        return forward(apiEndPointPath, HttpMethod.DELETE, null);
    }

    /**
     * Evaluates an authorization request.
     * Uses default user and resource types.
     */
    @Override
    public boolean evaluate(
            String storeId,
            String user,
            String resource,
            String action) throws Exception {
        return evaluate(
                storeId,
                Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE,
                user,
                Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE,
                resource,
                action
        );
    }

    /**
     * Evaluates an authorization request with explicit subject and resource types.
     */
    @Override
    public boolean evaluate(
            String storeId,
            String subjectType,
            String subjectId,
            String resourceType,
            String resourceId,
            String action) throws Exception {
        // Build the payload for evaluation
        String apiEndPointPath = String.join("/", ConstPath.AUTHORIZATION_EVALUATION.replace("{" + Const.API_PATH_PARAM_STORE_ID + "}", storeId));

        // Build the EvaluateRequest object
        EvaluateRequest evaluateRequest = new EvaluateRequest(
                subjectType,
                subjectId,
                resourceType,
                resourceId,
                action,
                LocalDateTime.now(ZoneOffset.UTC)
        );
        String payload = objectMapper.writeValueAsString(evaluateRequest);

        // Forward the request and parse the response
        ResponseEntity<Object> responseEntity = forward(apiEndPointPath, HttpMethod.POST, payload);

        // Convert the response to EvaluateResponse
        EvaluateResponse evaluateResponse = objectMapper.convertValue(responseEntity.getBody(), EvaluateResponse.class);
        // Validate the EvaluateResponse object
        Set<ConstraintViolation<EvaluateResponse>> violations = validator.validate(evaluateResponse);
        if (!violations.isEmpty()) {
            // If there are validation errors, throw a UnexpectedException
            throw new UnexpectedException(ConstError.ERR_500, ErrorDetails.createErrorDetails(violations));
        }
        return evaluateResponse.getDecision();
    }

    /**
     * Finds a store by its ID.
     */
    @Override
    public ResponseEntity<Object> findStoreById(String storeId) {
        String apiEndPointPath = String.join("/", ConstPath.AUTHORIZATION_STORES_PATH, storeId);
        return forward(apiEndPointPath, HttpMethod.GET, null);
    }

    /**
     * Writes tuples to the authorization store.
     */
    @Override
    public void writeTuples(List<Tuple> tuples, String storeId) throws Exception {
        Writes writes = new Writes(tuples);
        TupleWriteRequest tupleWriteRequest = new TupleWriteRequest(writes);

        String payload = objectMapper.writeValueAsString(tupleWriteRequest);
        String apiPath = ConstPath.AUTHORIZATION_TUPLES_WRITE.replace("{" + Const.API_PATH_PARAM_STORE_ID + "}", storeId);

        forward(apiPath, HttpMethod.POST, payload);
    }

    /**
     * Deletes tuples to the authorization store.
     */
    @Override
    public void deleteTuples(List<Tuple> tuples, String storeId) {
        Deletes deletes = new Deletes(tuples);
        TupleDeleteRequest tupleDeleteRequest = new TupleDeleteRequest(deletes);

        String payload;
        try {
            payload = objectMapper.writeValueAsString(tupleDeleteRequest);
        } catch (Exception e) {
            throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
        }
        String apiPath = ConstPath.AUTHORIZATION_TUPLES_WRITE.replace("{" + Const.API_PATH_PARAM_STORE_ID + "}", storeId);
        forward(apiPath, HttpMethod.POST, payload);
    }

    /**
     * Evaluates multiple resources in a single request using OpenFGA evaluations endpoint.
     */
    @Override
    public List<Boolean> evaluations(String storeId, String user, String action, List<Map<String, String>> resources) throws Exception {
        String apiEndPointPath = String.join("/", ConstPath.AUTHORIZATION_EVALUATIONS.replace("{" + Const.API_PATH_PARAM_STORE_ID + "}", storeId));

        // Batch size (default 50, configurable via OpenFgaProperties)
        int batchSize = Math.max(1, Const.EVALUATIONS_BATCH_SIZE_MAX);

        List<Boolean> decisions = new ArrayList<>(resources.size());

        // Process in chunks to respect service limits and avoid payload errors
        for (int start = 0; start < resources.size(); start += batchSize) {
            int end = Math.min(start + batchSize, resources.size());
            List<Map<String, String>> sub = resources.subList(start, end);

            // Build DTO request for the current chunk
            EvaluationsRequest req = new EvaluationsRequest(user, action);
            for (Map<String, String> res : sub) {
                req.addEvaluation(res.getOrDefault(Const.OPENFGA_EVALUATION_DEFAULT_KEY, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE), res.get(Const.OPENFGA_EVALUATION_DEFAULT_ID));
            }

            String payload = objectMapper.writeValueAsString(req);
            ResponseEntity<Object> responseEntity = forward(apiEndPointPath, HttpMethod.POST, payload);

            // Convert and validate response for the chunk
            EvaluationsResponse resp = objectMapper.convertValue(responseEntity.getBody(), EvaluationsResponse.class);
            Set<ConstraintViolation<EvaluationsResponse>> violations = validator.validate(resp);
            if (!violations.isEmpty()) {
                throw new UnexpectedException(ConstError.ERR_500, ErrorDetails.createErrorDetails(violations));
            }

            if (resp.getEvaluations() != null) {
                for (EvaluationDecision d : resp.getEvaluations()) {
                    decisions.add(Boolean.TRUE.equals(d.getDecision()));
                }
            }
        }
        return decisions;
    }

    /**
     * Throws exceptions based on HTTP error status codes.
     *
     * @param responseEntity ResponseEntity from the HTTP request
     */
    private void throwIfErrorStatus(ResponseEntity<String> responseEntity) {
        HttpStatusCode status = responseEntity.getStatusCode();
        if (status.is2xxSuccessful()) {
            return; // No error
        }

        // Prepare error message body
        String bodyJsonString = responseEntity.getBody();
        String errorMessage = ConstError.ERRLOG_RESPONSE_BODY_NO_DATA;
        if (StringUtils.hasText(bodyJsonString)) {
            try {
                // Parse the JSON error message into a map
                Map<String, Object> map = objectMapper.readValue(bodyJsonString, new TypeReference<Map<String, Object>>() {});
                errorMessage = map.entrySet().stream()
                        .map(e -> "%s: %s".formatted(e.getKey(), String.valueOf(e.getValue())))
                        .collect(Collectors.joining("; "));
            } catch (Exception e) {
                throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
            }
        }

        // Handle specific HTTP error statuses
        switch (status) {
            case HttpStatus.BAD_REQUEST:
                throw new BadParametersException(String.format(ConstError.ERR_400_BAD_REQUEST_MESSAGE, errorMessage));
            case HttpStatus.CONFLICT:
                throw new ConflictException(String.format(ConstError.ERR_409_CONFLICT_MESSAGE, errorMessage));
            case HttpStatus.NOT_FOUND:
                throw new NotFoundException(String.format(ConstError.ERR_404_RESOURCE_NOT_FOUND_MESSAGE, errorMessage));
            case HttpStatus.SERVICE_UNAVAILABLE:
                throw new OutOfServiceException(String.format(ConstError.ERR_503_OUTER_SERVICE_EXCEPTION, errorMessage));
            default:
                throw new UnexpectedException(ConstError.ERR_500, errorMessage);
        }
    }
}
