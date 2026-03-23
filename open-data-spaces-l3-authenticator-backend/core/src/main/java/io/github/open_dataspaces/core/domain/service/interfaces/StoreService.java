/*
 * StoreService.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Service interface for store operations.
 *
 * Date: 2026/02/09
 */

package io.github.open_dataspaces.core.domain.service.interfaces;

import org.springframework.http.ResponseEntity;

import io.micrometer.common.lang.NonNull;

/**
 * Service interface for store operations.
 */
public interface StoreService {

    /**
     * Registers a store and creates a binding.
     *
     * @param apiKey The API key
     * @param rawJson The raw JSON request body
     * @return ResponseEntity containing the created store information
     */
    public @NonNull ResponseEntity<Object> registerStore(
            @NonNull String apiKey,
            @NonNull String rawJson
    ) throws Exception;

    /**
     * Deletes a store and removes the binding.
     *
     * @param apiKey The API key
     * @param storeId The ID of the store to delete
     */
    public void deleteStore(
            @NonNull String apiKey,
            @NonNull String storeId
    );
}
