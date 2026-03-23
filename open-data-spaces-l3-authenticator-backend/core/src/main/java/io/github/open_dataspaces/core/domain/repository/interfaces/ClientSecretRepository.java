/*
 * ClientSecretRepository.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This repository is a Spring Data JPA repository interface for ClientSecretEntity.
 *
 * Date: 2025/11/30
 */

package io.github.open_dataspaces.core.domain.repository.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.github.open_dataspaces.core.domain.entities.ClientSecretEntity;

/**
 * Repository interface for accessing operator entities.
 *
 * <p>Provides methods to query operators by operatorId, openOperatorId, and a list of operatorIds.</p>
 */
@Repository
public interface ClientSecretRepository extends JpaRepository<ClientSecretEntity, String> {

    /**
     * Persists a new relation row linking a Keycloak-issued client UUID to the API key that
     * initiated its creation. Overwrites nothing because {@code client_uuid} is the primary key;
     * attempting to re-save the same UUID will raise a constraint violation.
     *
     * @param clientUuid the client UUID (primary key)
     * @param apiKey the API key from request headers
     * @param createdUserId operator/open system ID extracted from JWT (may be blank)
     */
    default void save(String clientUuid, String apiKey, String createdUserId) {
        save(new ClientSecretEntity(clientUuid, apiKey, createdUserId));
    }

    /**
     * Checks whether a relation row exists for the specified client UUID.
     *
     * <p>Used to enforce “only clients created through this API may retrieve their secret once”.</p>
     *
     * @param clientUuid the client UUID to check
     * @return true if a row exists; false otherwise
     */
    boolean existsByClientUuid(String clientUuid);

    /**
     * Deletes the relation row for the specified client UUID. Silent when the row does not exist
     * (idempotent behavior).
     *
     * @param clientUuid the client UUID whose row should be removed
     */
    void deleteByClientUuid(String clientUuid);
}