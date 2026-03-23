/*
 * APIKeysRepository.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This repository is a Spring Data JPA repository interface for APIKeysEntity.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.repository.interfaces;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;
import io.github.open_dataspaces.core.domain.entities.APIKeysEntity;

/**
 * Repository interface for accessing API key entities.
 *
 * <p>Provides methods to query API keys by attributes and API key value.</p>
 */
public interface APIKeysRepository extends JpaRepository<APIKeysEntity, String> {

    /**
     * Finds API keys by API key value, excluding deleted records.
     *
     * @param apiKey the API key value
     * @return a list of matching APIKeysEntity objects
     */
    @Query(ConstSqlQueries.SELECT_APIKEYS)
    @NonNull
    List<APIKeysEntity> findByApiKey(@NonNull @Param("apiKey") String apiKey, @NonNull @Param("currentDate") LocalDate currentDate);
}
