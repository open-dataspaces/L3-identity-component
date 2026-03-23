/*
 * AuthorizationService.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Service interface for authorization operations.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.domain.service.interfaces;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import io.micrometer.common.lang.NonNull;
import io.micrometer.common.lang.Nullable;

import java.util.List;
import java.util.Map;

import io.github.open_dataspaces.core.infrastructure.dto.Tuple;

/**
 * Service interface for authorization operations.
 */
public interface AuthorizationService {

    /**
     * Forwards a JSON request to the authorization service.
     *
     * @param apiEndPointPath The API endpoint path to forward the request to
     * @param httpMethod The HTTP method of the request
     * @param payload The request payload, if any
     * @return The response entity containing the response from the authorization service
     */
    public @NonNull ResponseEntity<Object> forward(
            @NonNull String apiEndPointPath,
            @NonNull HttpMethod httpMethod,
            @Nullable String payload
    );

    /**
     * Deletes a store in the authorization service.
     *
     * @param storeId The store ID to delete
     * @return The response entity from the authorization service
     */
    public @NonNull ResponseEntity<Object> deleteStore(@NonNull String storeId);

    /**
     * Evaluates an authorization request.
     * Uses default user and resource types.
     *
     * @param storeId The ID of the store
     * @param user The user making the request
     * @param resource The resource being accessed
     * @param action The action being requested
     * @return true if the action is authorized, false otherwise
     * @throws Exception if an error occurs during evaluation
     */
    public boolean evaluate(
            @NonNull String storeId,
            @NonNull String user,
            @NonNull String resource,
            @NonNull String action
    ) throws Exception;

    /**
     * Evaluates an authorization request with explicit subject and resource types.
     *
     * @param storeId The ID of the store
     * @param subjectType The type of the subject (e.g., "user", "realm")
     * @param subjectId The ID of the subject
     * @param resourceType The type of the resource (e.g., "api", "store")
     * @param resourceId The ID of the resource
     * @param action The action being requested
     * @return true if the action is authorized, false otherwise
     * @throws Exception if an error occurs during evaluation
     */
    public boolean evaluate(
            @NonNull String storeId,
            @NonNull String subjectType,
            @NonNull String subjectId,
            @NonNull String resourceType,
            @NonNull String resourceId,
            @NonNull String action
    ) throws Exception;

    /**
     * Evaluates multiple resources in a single request.
     *
     * @param storeId The ID of the store
     * @param user The user making the request
     * @param action The action being requested
     * @param resources A list of resource descriptors represented as simple maps with keys "type" and "id"
     * @return A list of booleans indicating decision for each resource in the same order
     * @throws Exception if an error occurs during evaluation
     */
    public List<Boolean> evaluations(
            @NonNull String storeId,
            @NonNull String user,
            @NonNull String action,
            @NonNull List<Map<String, String>> resources
    ) throws Exception;

    /**
     * Finds a store by its ID.
     *
     * @param storeId The ID of the store to find.
     * @return ResponseEntity containing the store information
     */
    public @NonNull ResponseEntity<Object> findStoreById(@NonNull String storeId);

    /**
     * Writes tuples to the authorization store.
     *
     * @param tuples The list of tuples to write
     * @param storeId The ID of the store
     * @throws Exception if the tuple write operation fails
     */
    public void writeTuples(
            @NonNull List<Tuple> tuples,
            @NonNull String storeId
    ) throws Exception;

    /**
     * Deletes tuples from the authorization store.
     *
     * @param tuples The list of tuples to delete
     * @param storeId The ID of the store
     * @throws Exception if the tuple delete operation fails
     */
    public void deleteTuples(
            @NonNull List<Tuple> tuples,
            @NonNull String storeId
    );
}
