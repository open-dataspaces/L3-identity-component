/*
 * OperatorRepositoryCustom.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This is a custom repository interface for OperatorEntity.
 *
 * Date: 2025/08/30
 */

package io.github.open_dataspaces.core.domain.repository.interfaces;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import io.github.open_dataspaces.core.domain.entities.OperatorEntity;

/**
 * Custom repository interface for OperatorEntity.
 */
public interface OperatorRepositoryCustom {

    /**
     * Retrieves a list of operators matching the given specification and pagination information.
     *
     * @param spec the specification to filter the operators
     * @param offset the offset for pagination
     * @param limit the maximum number of results to return
     * @param sort the sorting information
     * @return a list of OperatorEntity objects matching the specification
     */
    @NonNull
    List<OperatorEntity> findWithLimitOffset(Specification<OperatorEntity> spec, int offset, int limit, Sort sort);

}