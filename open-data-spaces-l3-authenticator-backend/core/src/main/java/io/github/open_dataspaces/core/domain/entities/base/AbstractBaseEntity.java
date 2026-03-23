/*
 * AbstractBaseEntity.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a base entity for all entities in the application.
 *
 * Date: 2025/08/04
 */

package io.github.open_dataspaces.core.domain.entities.base;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;
import io.github.open_dataspaces.core.common.utils.DateUtils;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import lombok.Data;

/**
 * BaseEntity is a base class for all entities in the application that require common fields.
 * It includes fields for tracking deletion status, effective dates, and timestamps for creation
 * and updates.
 */
@MappedSuperclass
@Data
public abstract class AbstractBaseEntity {

    /* ------------- common column ----------------- */
    @Column(name = ConstSqlQueries.COLUMN_COMMON_DELETED_FLAG)
    private boolean deletedFlag;

    @Column(name = ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_START_DATE)
    private LocalDate effectiveStartDate;

    @Column(name = ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_END_DATE)
    private LocalDate effectiveEndDate;

    @Column(name = ConstSqlQueries.COLUMN_COMMON_CREATED_AT)
    private LocalDateTime createdAt;

    @Column(name = ConstSqlQueries.COLUMN_COMMON_CREATED_USER_ID)
    private String createdUserId;

    @Column(name = ConstSqlQueries.COLUMN_COMMON_UPDATED_AT)
    private LocalDateTime updatedAt;

    @Column(name = ConstSqlQueries.COLUMN_COMMON_UPDATED_USER_ID)
    private String updatedUserId;
    /* ------------- common column ----------------- */

    /**
     * Default constructor for BaseEntity.
     * Initializes the effective dates to empty strings and timestamps to null.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS);
        updatedAt = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS);
    }

    /**
     * Updates the timestamps before persisting or updating the entity.
     * This method is called automatically by JPA when the entity is saved or updated.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS);
    }

    /**
     * Checks if the status fields have been updated.
     *
     * @param effectiveStartDate account effective start date
     * @param effectiveEndDate account effective end date
     * @param deletedFlag account deleted flag
     * @return true if any of the status fields have been updated, false otherwise
     */
    public boolean isStatusUpdated(LocalDate effectiveStartDate, LocalDate effectiveEndDate, Boolean deletedFlag) {
        if (effectiveStartDate != null && !DateUtils.equals(this.effectiveStartDate, effectiveStartDate, Const.DATE_FORMAT)) {
            return true;
        }
        if (effectiveEndDate != null && !DateUtils.equals(this.effectiveEndDate, effectiveEndDate, Const.DATE_FORMAT)) {
            return true;
        }
        if (deletedFlag != null && !Objects.equals(this.deletedFlag, deletedFlag)) {
            return true;
        }
        return false;

    }

}