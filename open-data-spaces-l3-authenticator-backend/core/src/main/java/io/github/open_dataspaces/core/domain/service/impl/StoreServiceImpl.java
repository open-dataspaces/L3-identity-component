/*
 * StoreServiceImpl.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Service implementation for store operations.
 *
 * Date: 2026/02/09
 */

package io.github.open_dataspaces.core.domain.service.impl;

import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.common.enums.EnumStorePurpose;
import io.github.open_dataspaces.core.domain.repository.interfaces.AuthorizationStoresRepository;
import io.github.open_dataspaces.core.domain.repository.interfaces.AuthorizationUCStoresRepository;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.domain.service.interfaces.StoreService;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.infrastructure.dto.CreateStoreResponse;
import io.github.open_dataspaces.core.infrastructure.dto.Tuple;
import io.github.open_dataspaces.core.infrastructure.utils.TupleUtils;

import io.micrometer.common.lang.NonNull;

/**
 * Service implementation for store operations.
 */
@Service
public class StoreServiceImpl implements StoreService {

    private final AuthorizationService authorizationService;
    private final AuthorizationStoresRepository authorizationStoresRepository;
    private final AuthorizationUCStoresRepository authorizationUCStoresRepository;
    private final APIKeyService apiKeyService;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new StoreServiceImpl with the specified dependencies.
     *
     * @param authorizationService the service for authorization operations
     * @param authorizationStoresRepository the repository for authorization stores
     * @param authorizationUCStoresRepository the repository for UC authorization stores
     * @param apiKeyService the service for API key operations
     * @param objectMapper the object mapper for JSON processing
     */
    public StoreServiceImpl(
            AuthorizationService authorizationService,
            AuthorizationStoresRepository authorizationStoresRepository,
            AuthorizationUCStoresRepository authorizationUCStoresRepository,
            APIKeyService apiKeyService, ObjectMapper objectMapper) {
        this.authorizationService = authorizationService;
        this.authorizationStoresRepository = authorizationStoresRepository;
        this.authorizationUCStoresRepository = authorizationUCStoresRepository;
        this.apiKeyService = apiKeyService;
        this.objectMapper = objectMapper;
    }

    /**
     * Registers a store and creates a binding.
     */
    @Override
    public @NonNull ResponseEntity<Object> registerStore(
            @NonNull String apiKey,
            @NonNull String rawJson
    ) throws Exception {
        // Create authorization store
        ResponseEntity<Object> createResult = authorizationService.forward(ConstPath.AUTHORIZATION_STORES_PATH, HttpMethod.POST, rawJson);

        // Extract store ID from response
        String createdStoreId = objectMapper.convertValue(createResult.getBody(), CreateStoreResponse.class).getId();

        // Register store binding
        try {
            String realm = apiKeyService.getIdpRealm(apiKey);
            String adminStoreId = apiKeyService.getStoreId(apiKey, EnumStorePurpose.REALM_STORE_BINDING.name());
            List<Tuple> tuples = TupleUtils.buildRealmStoreBindingTuples(realm, createdStoreId);
            authorizationService.writeTuples(tuples, adminStoreId);
        } catch (Exception e) {
            try {
                // Compensating action: rollback the created store
                authorizationService.deleteStore(createdStoreId);
            } catch (Exception re) {
                e.addSuppressed(re);
            }
            throw e;
        }

        return createResult;
    }

    /**
     * Deletes a store and removes the binding.
     */
    @Override
    public void deleteStore(
            @NonNull String apiKey,
            @NonNull String storeId
    ) {
        // Check if store exists in the database (Authorization Stores or UC Authorization Stores)
        if (authorizationStoresRepository.existsById(storeId) || authorizationUCStoresRepository.existsById(storeId)) {
            throw new ConflictException(String.format(ConstError.ERRLOG_409_STORE_CANNOT_BE_DELETED, storeId));
        }

        // Delete authorization store
        authorizationService.deleteStore(storeId);

        // Delete store binding
        String realm = apiKeyService.getIdpRealm(apiKey);
        String adminStoreId = apiKeyService.getStoreId(apiKey, EnumStorePurpose.REALM_STORE_BINDING.name());
        List<Tuple> tuples = TupleUtils.buildRealmStoreBindingTuples(realm, storeId);
        authorizationService.deleteTuples(tuples, adminStoreId);
    }
}
