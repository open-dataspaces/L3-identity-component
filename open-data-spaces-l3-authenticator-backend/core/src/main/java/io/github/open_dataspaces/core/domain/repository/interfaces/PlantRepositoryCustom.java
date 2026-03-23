/*
 * PlantRepositoryCustom.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This repository is a Spring Data JPA repository interface for PlantEntity.
 *
 * Date: 2025/09/11
 */

package io.github.open_dataspaces.core.domain.repository.interfaces;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import io.github.open_dataspaces.core.domain.entities.PlantEntity;

/**
 * Custom repository interface for PlantEntity.
 */
public interface PlantRepositoryCustom {

    /**
     * Retrieves a list of plants matching the given specification and pagination information.
     *
     * @param spec the specification to filter the plants
     * @param offset the offset for pagination
     * @param limit the maximum number of results to return
     * @param sort the sorting information
     * @return a list of PlantEntity objects matching the specification
     */
    List<PlantEntity> findWithLimitOffset(Specification<PlantEntity> spec, int offset, int limit, Sort sort);

}