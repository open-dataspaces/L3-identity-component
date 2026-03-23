/*
 * ClientSecretServiceImpl.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This service provides the implementation for client secret management logic.
 *
 * Date: 2025/11/30
 */

package io.github.open_dataspaces.core.domain.service.impl;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.open_dataspaces.core.domain.repository.interfaces.ClientSecretRepository;
import io.github.open_dataspaces.core.domain.service.interfaces.ClientSecretService;

/**
 * Service implementation that orchestrates persistence of client–API key relations.
 *
 * <p>Delegates to the repository; exceptions propagate upward.
 */
@Service
public class ClientSecretServiceImpl implements ClientSecretService {

    private final ClientSecretRepository repository;

    /**
     * Constructs this service with the repository dependency required for persistence.
     * No additional initialization logic is performed.
     */
    public ClientSecretServiceImpl(ClientSecretRepository repository) {
        this.repository = repository;
    }

    /**
     * Persists a link between a client UUID and the API key that initiated its creation.
     * Exceptions from the repository layer propagate upward.
     */
    @Override
    @Transactional
    public void save(@NonNull String clientUuid, @NonNull String apiKey, String createdUserId) {
        repository.save(clientUuid, apiKey, createdUserId);
    }

    /**
     * Determines whether a link row exists for the specified client UUID.
     * Used to enforce one-time retrieval semantics before returning the secret.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(@NonNull String clientUuid) {
        return repository.existsByClientUuid(clientUuid);
    }

    /**
     * Deletes the link row for the given client UUID after successful secret retrieval.
     * Silent if the row does not exist (idempotent behavior).
     */
    @Override
    @Transactional
    public void deleteById(@NonNull String clientUuid) {
        repository.deleteByClientUuid(clientUuid);
    }
}