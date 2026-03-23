/*
 * OperatorResult.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for operator records.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.github.open_dataspaces.core.domain.entities.OperatorEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for operator records in the database.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperatorResult {

    /**
     * The operator ID.
     */
    private String operatorId;

    /**
     * The login user ID.
     */
    private String operatorName;

    /**
     * The email address.
     */
    private String operatorAddress;

    /**
     * The OpenID Connect operator ID.
     */
    private String openOperatorId;

    /**
     * The global operator ID.
     */
    private String globalOperatorId;

    /**
     * The deletion flag.
     */
    private boolean deletedFlag;

    /**
     * The effective start date.
     */
    private LocalDate effectiveStartDate;

    /**
     * The effective end date.
     */
    private LocalDate effectiveEndDate;

    /**
     * The creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * The ID of the user who created the record.
     */
    private String createdUserId;

    /**
     * The last update timestamp.
     */
    private LocalDateTime updatedAt;

    /**
     * The ID of the user who last updated the record.
     */
    private String updatedUserId;

    /**
     * Constructs an OperatorResult from an OperatorEntity.
     *
     * @param operatorEntity the OperatorEntity to convert
     */
    public OperatorResult(OperatorEntity operatorEntity) {
        this.operatorId = operatorEntity.getOperatorId();
        this.operatorName = operatorEntity.getOperatorName();
        this.operatorAddress = operatorEntity.getOperatorAddress();
        this.openOperatorId = operatorEntity.getOpenOperatorId();
        this.globalOperatorId = operatorEntity.getGlobalOperatorId();
        this.deletedFlag = operatorEntity.isDeletedFlag();
        this.effectiveStartDate = operatorEntity.getEffectiveStartDate();
        this.effectiveEndDate = operatorEntity.getEffectiveEndDate();
        this.createdAt = operatorEntity.getCreatedAt();
        this.createdUserId = operatorEntity.getCreatedUserId();
        this.updatedAt = operatorEntity.getUpdatedAt();
        this.updatedUserId = operatorEntity.getUpdatedUserId();
    }
}