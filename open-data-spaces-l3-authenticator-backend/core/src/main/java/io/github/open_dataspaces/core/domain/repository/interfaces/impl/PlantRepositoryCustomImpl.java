/*
 * PlantRepositoryCustomImpl.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This repository is a Spring Data JPA repository interface for PlantEntity.
 *
 * Date: 2025/09/11
 */

package io.github.open_dataspaces.core.domain.repository.interfaces.impl;   // Custom implementation classes should be placed in the impl subpackage

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;

import io.github.open_dataspaces.core.domain.entities.PlantEntity;
import io.github.open_dataspaces.core.domain.repository.interfaces.PlantRepositoryCustom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 * Custom implementation of the PlantRepository interface.
 */
public class PlantRepositoryCustomImpl implements PlantRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Finds a list of PlantEntity with pagination and sorting.
     *
     * @param spec the specification for filtering
     * @param offset the offset for pagination
     * @param limit the limit for pagination
     * @param sort the sorting information
     * @return a list of PlantEntity
     */
    @Override
    public List<PlantEntity> findWithLimitOffset(Specification<PlantEntity> spec, int offset, int limit, Sort sort) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PlantEntity> query = criteriaBuilder.createQuery(PlantEntity.class);
        Root<PlantEntity> root = query.from(PlantEntity.class);
        query.select(root);

        if (spec != null) {
            var predicate = spec.toPredicate(root, query, criteriaBuilder);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        query.orderBy(QueryUtils.toOrders(sort, root, criteriaBuilder));

        TypedQuery<PlantEntity> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(limit > 0 ? limit : Integer.MAX_VALUE);

        return typedQuery.getResultList();
    }

}