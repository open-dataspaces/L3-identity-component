/*
 * AuthorizationStoresRepository.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Repository interface for managing authorization store entities.
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.domain.repository.interfaces;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.github.open_dataspaces.core.domain.entities.AuthorizationStoresEntity;

/**
 * Repository interface for managing authorization store entities.
 */
@Repository
public interface AuthorizationStoresRepository extends JpaRepository<AuthorizationStoresEntity, String> {

    /**
     * Find an authorization store by environment name, IDP realm, and store purpose.
     */
    public Optional<AuthorizationStoresEntity> findByEnvironmentNameAndIdpRealmAndPdpStorePurpose(
            String environmentName, String idpRealm, String pdpStorePurpose);
}
