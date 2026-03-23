/*
 * GetPlantResponse.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a DTO for plant information responses.
 *
 * Date: 2025/09/11
 */

package io.github.open_dataspaces.core.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.open_dataspaces.core.common.consts.Const;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for plant information responses.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetPlantResponse {

    @JsonProperty(Const.JSON_PROPERTY_OPERATOR_ID)
    private String operatorId;

    @JsonProperty(Const.JSON_PROPERTY_PLANT_ID)
    private String plantId;

    @JsonProperty(Const.JSON_PROPERTY_OPEN_PLANT_ID)
    private String openPlantId;

    @JsonProperty(Const.JSON_PROPERTY_GLOBAL_PLANT_ID)
    private String globalPlantId;

    @JsonProperty(Const.JSON_PROPERTY_PLANT_NAME)
    private String plantName;

    @JsonProperty(Const.JSON_PROPERTY_PLANT_ADDRESS)
    private String plantAddress;

    @JsonProperty(Const.JSON_PROPERTY_EFFECTIVE_START_DATE)
    @JsonFormat(pattern = Const.DATE_FORMAT)
    private LocalDate effectiveStartDate;

    @JsonProperty(Const.JSON_PROPERTY_EFFECTIVE_END_DATE)
    @JsonFormat(pattern = Const.DATE_FORMAT)
    private LocalDate effectiveEndDate;

    @JsonProperty(Const.JSON_PROPERTY_DELETED_FLAG)
    private Boolean deletedFlag;

    @JsonProperty(Const.JSON_PROPERTY_CREATED_AT)
    @JsonFormat(pattern = Const.ISO_8601_UTC_MILLISECOND_FORMAT)
    private LocalDateTime createdAt;

    @JsonProperty(Const.JSON_PROPERTY_UPDATED_AT)
    @JsonFormat(pattern = Const.ISO_8601_UTC_MILLISECOND_FORMAT)
    private LocalDateTime updatedAt;

    /**
     * Constructs a new {@code GetPlantResponse} with the specified plant result.
     *
     *
     * @param plantResult the plant result containing the plant information
     *
     */
    public GetPlantResponse(PlantResult plantResult) {
        this.operatorId = plantResult.getOperatorId();
        this.openPlantId = plantResult.getOpenPlantId();
        this.globalPlantId = plantResult.getGlobalPlantId();
        this.plantId = plantResult.getPlantId();
        this.plantName = plantResult.getPlantName();
        this.plantAddress = plantResult.getPlantAddress();
        this.effectiveStartDate = plantResult.getEffectiveStartDate();
        this.effectiveEndDate = plantResult.getEffectiveEndDate();
        this.deletedFlag = plantResult.isDeletedFlag();
        this.createdAt = plantResult.getCreatedAt();
        this.updatedAt = plantResult.getUpdatedAt();
    }
}
