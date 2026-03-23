/*
 * APIKeyService.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines the interface for API key verification logic.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.service.interfaces;

import org.springframework.lang.NonNull;

import io.github.open_dataspaces.core.domain.dto.APIKeyVerifyParam;
import io.github.open_dataspaces.core.exception.ForbiddenException;
import io.github.open_dataspaces.core.exception.UnexpectedException;

/**
 * Service interface for API key verification operations.
 */
public interface APIKeyService {

    /**
     * Verify the API key and IP address, and return an error if validation fails.
     *
     * @param param the API key verification parameters
     * @return true if the API key is valid for the given parameters, false otherwise
     */
    boolean verify(@NonNull APIKeyVerifyParam param);

    /**
     * Verify only the API key, and return false if validation fails.
     *
     * @param apikey the API key verification parameters
     * @return true if the API key is valid for the given parameters, false otherwise
     */
    boolean verify(@NonNull String apikey);

    /**
     * Retrieves the IDP realm associated with the given API key.
     *
     * @param apiKey the API key to look up
     * @return the IDP realm associated with the API key, or null if not found
     */
    String getIdpRealm(@NonNull String apiKey);

    /**
     * Retrieves the store ID associated with the given API key.
     *
     * @param apiKey the API key to look up
     * @param storePurpose the store purpose to filter by
     * @return the store ID associated with the API key, or null if not found
     * @throws UnexpectedException if the store ID cannot be found for the given API key.
     * @throws ForbiddenException if the API key is not authorized to access the store ID.
     */
    @NonNull String getStoreId(@NonNull String apiKey, @NonNull String storePurpose)
        throws UnexpectedException, ForbiddenException;

    /**
     * Retrieves the usecase associated with the given API key.
     *
     * @param apiKey the API key to look up
     * @return the usecase associated with the API key
     */
    @NonNull String getUsecase(@NonNull String apiKey)
        throws UnexpectedException, ForbiddenException;

    /**
     * Retrieves the usecase store ID associated with the given API key.
     * This value is mandatory for operator/plant authorization flows; if missing an exception is thrown.
     *
     * @param apiKey the API key to look up
     * @return the usecase store ID associated with the API key
     */
    @NonNull String getUsecaseStoreId(@NonNull String apiKey)
        throws UnexpectedException, ForbiddenException;

    /**
     * Retrieves the usecase store name associated with the given API key.
     * This value is mandatory for operator/plant authorization flows; if missing an exception is thrown.
     *
     * @param apiKey the API key to look up
     * @return the usecase store name associated with the API key
     */
    @NonNull String getUsecaseStoreName(@NonNull String apiKey)
        throws UnexpectedException, ForbiddenException;

}
