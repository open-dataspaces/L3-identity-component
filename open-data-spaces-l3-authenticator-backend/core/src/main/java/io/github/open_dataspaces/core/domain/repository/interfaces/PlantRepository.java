/*
 * PlantRepository.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This repository is a Spring Data JPA repository interface for PlantEntity.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.repository.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.github.open_dataspaces.core.domain.entities.PlantEntity;

/**
 * Repository interface for accessing plant entities.
 *
 * <p>Provides methods to query plants by plantId, operatorId, and to list plants for a specific
 * operator.</p>
 */
@Repository
public interface PlantRepository extends JpaRepository<PlantEntity, String>, PlantRepositoryCustom {

}
