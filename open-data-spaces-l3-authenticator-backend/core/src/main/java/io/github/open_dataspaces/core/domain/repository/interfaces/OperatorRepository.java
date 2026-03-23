/*
 * OperatorRepository.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This repository is a Spring Data JPA repository interface for OperatorEntity.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.repository.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import io.github.open_dataspaces.core.domain.entities.OperatorEntity;

/**
 * Repository interface for accessing operator entities.
 *
 * <p>Provides methods to query operators by operatorId, openOperatorId, and a list of operatorIds.</p>
 */
@Repository
public interface OperatorRepository extends JpaRepository<OperatorEntity, String>, JpaSpecificationExecutor<OperatorEntity>, OperatorRepositoryCustom {

}
