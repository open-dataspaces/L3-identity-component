/*
 * PlantResult.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for plant records.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.github.open_dataspaces.core.domain.entities.PlantEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for operator records in the database.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlantResult {

    /**
     * The plant ID.
     */
    private String plantId;

    /**
     * The operator ID to which the plant belongs.
     */
    private String operatorId;

    /**
     * The plant name.
     */
    private String plantName;

    /**
     * The plant address.
     */
    private String plantAddress;

    /**
     * The OpenID Connect plant ID.
     */
    private String openPlantId;

    /**
     * The global plant ID.
     */
    private String globalPlantId;

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
     * Constructs a PlantResult from a PlantEntity.
     *
     * @param plantEntity the PlantEntity to convert
     */
    public PlantResult(PlantEntity plantEntity) {
        this.plantId = plantEntity.getPlantId();
        this.operatorId = plantEntity.getOperatorId();
        this.plantName = plantEntity.getPlantName();
        this.plantAddress = plantEntity.getPlantAddress();
        this.openPlantId = plantEntity.getOpenPlantId();
        this.globalPlantId = plantEntity.getGlobalPlantId();
        this.deletedFlag = plantEntity.isDeletedFlag();
        this.effectiveStartDate = plantEntity.getEffectiveStartDate();
        this.effectiveEndDate = plantEntity.getEffectiveEndDate();
        this.createdAt = plantEntity.getCreatedAt();
        this.createdUserId = plantEntity.getCreatedUserId();
        this.updatedAt = plantEntity.getUpdatedAt();
        this.updatedUserId = plantEntity.getUpdatedUserId();
    }
}