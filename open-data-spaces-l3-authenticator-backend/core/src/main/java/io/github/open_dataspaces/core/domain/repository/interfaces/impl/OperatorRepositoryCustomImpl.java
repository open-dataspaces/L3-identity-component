/*
 * OperatorRepositoryCustomImpl.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This repository is a Spring Data JPA repository interface for OperatorEntity.
 *
 * Date: 2025/09/03
 */

package io.github.open_dataspaces.core.domain.repository.interfaces.impl;   // Custom classes must be placed in the interfaces subpackage

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;

import io.github.open_dataspaces.core.domain.entities.OperatorEntity;
import io.github.open_dataspaces.core.domain.repository.interfaces.OperatorRepositoryCustom;

/**
 * Custom implementation of the OperatorRepository interface.
 */
public class OperatorRepositoryCustomImpl implements OperatorRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Finds a list of OperatorEntity with pagination and sorting.
     *
     * @param spec the specification for filtering
     * @param offset the offset for pagination
     * @param limit the limit for pagination
     * @param sort the sorting information
     * @return a list of OperatorEntity
     */
    @Override
    public List<OperatorEntity> findWithLimitOffset(Specification<OperatorEntity> spec, int offset, int limit, Sort sort) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<OperatorEntity> query = criteriaBuilder.createQuery(OperatorEntity.class);
        Root<OperatorEntity> root = query.from(OperatorEntity.class);
        query.select(root);

        if (spec != null) {
            var predicate = spec.toPredicate(root, query, criteriaBuilder);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        query.orderBy(QueryUtils.toOrders(sort, root, criteriaBuilder));

        TypedQuery<OperatorEntity> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(limit > 0 ? limit : Integer.MAX_VALUE);

        return typedQuery.getResultList();
    }

}