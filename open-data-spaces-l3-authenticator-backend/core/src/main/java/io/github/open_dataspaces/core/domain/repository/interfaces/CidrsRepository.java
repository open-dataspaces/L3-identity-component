/*
 * CidrsRepository.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This repository is a Spring Data JPA repository interface for CidrsEntity.
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
import io.github.open_dataspaces.core.domain.entities.CidrsEntity;
import io.github.open_dataspaces.core.domain.entities.CidrsKey;

/**
 * Repository interface for accessing CIDR entities.
 *
 * <p>Provides methods to query CIDRs associated with API keys and IP addresses.</p>
 */
public interface CidrsRepository extends JpaRepository<CidrsEntity, CidrsKey> {

    /**
     * Finds CIDRs associated with a given API key and IP address.
     *
     * @param apiKey the API key to search for
     * @param ip the IP address to check against the CIDRs
     * @return a list of CidrsEntity objects that match the criteria
     */
    @Query(value = ConstSqlQueries.SELECT_CIDRS, nativeQuery = true)
    List<CidrsEntity> findCidrs(@NonNull @Param("apiKey") String apiKey,
            @NonNull @Param("ip") String ip, @NonNull @Param("currentDate") LocalDate currentDate);
}
