/*
 * ClientSecretService.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This service orchestrates persistence of client–API key associations.
 *
 * Date: 2025/11/26
 */

package io.github.open_dataspaces.core.domain.service.interfaces;

/**
 * Service interface for persisting relations between created clients and API keys.
 *
 * <p>Keeps controllers thin by centralizing the business intent to persist
 * client–API key associations into the local database.</p>
 */
public interface ClientSecretService {

    /**
     * Saves a client–API key relation into {@code auth.tbl_client_secrets}.
     *
     * @param clientUuid the UUID of the created client (issued by the IdP)
     * @param apiKey the API key used to initiate client creation
     * @param createdUserId the operator ID from JWT (the initiator); can be empty
     */
    void save(String clientUuid, String apiKey, String createdUserId);

    /**
     * Checks existence of a client–API key relation by client UUID.
     *
     * @param clientUuid the client UUID
     * @return true if exists; false otherwise
     */
    boolean existsById(String clientUuid);

    /**
     * Deletes a client–API key relation by client UUID.
     *
     * @param clientUuid the client UUID
     */
    void deleteById(String clientUuid);
}